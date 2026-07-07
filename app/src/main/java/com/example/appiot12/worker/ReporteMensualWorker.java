package com.example.appiot12.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.appiot12.model.Dispositivo;
import com.example.appiot12.model.TanqueAgua;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ReporteMensualWorker extends Worker {

    private static final String TAG = "ReporteMensualWorker";
    private static final long TREINTA_DIAS_MS = 30L * 24L * 60L * 60L * 1000L;

    private static final String CORREO_REPORTE = com.example.appiot12.BuildConfig.SMTP_EMAIL;
    private static final String CLAVE_APP_CORREO = com.example.appiot12.BuildConfig.SMTP_PASSWORD;

    public ReporteMensualWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando ReporteMensualWorker");

        try {
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

            if (usuario == null) {
                Log.e(TAG, "No hay usuario autenticado");
                return Result.retry();
            }

            String uid = usuario.getUid();
            String emailDestino = usuario.getEmail();

            if (emailDestino == null || emailDestino.trim().isEmpty()) {
                Log.e(TAG, "El usuario no tiene correo válido");
                return Result.failure();
            }

            DatabaseReference refTanques = FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).child("tanques");

            DataSnapshot snapshotTanques = Tasks.await(refTanques.get());

            if (!snapshotTanques.exists()) {
                Log.d(TAG, "No hay tanques para procesar");
                return Result.success();
            }

            boolean errorTemporal = false;

            for (DataSnapshot tanqueSnapshot : snapshotTanques.getChildren()) {
                TanqueAgua tanque = tanqueSnapshot.getValue(TanqueAgua.class);
                if (tanque == null) continue;

                if (tanque.getIdTanque() == null || tanque.getIdTanque().trim().isEmpty()) {
                    tanque.setIdTanque(tanqueSnapshot.getKey());
                }

                inicializarFechasSiHaceFalta(tanque);

                if (!debeEnviarReporte(tanque)) continue;

                List<Dispositivo> historial = obtenerHistorialDispositivo(uid, tanque);
                String resumen = generarResumenMensual(tanque, historial);
                String asunto = "Reporte mensual de tu tanque: " + obtenerNombreSeguroTanque(tanque);

                boolean enviado = enviarCorreo(emailDestino, asunto, resumen);

                if (enviado) {
                    tanque.marcarCorreoEnviado();
                    guardarUltimoCorreoEnviado(uid, tanque);
                    Log.d(TAG, "Correo enviado para tanque: " + tanque.getNombre());
                } else {
                    errorTemporal = true;
                    Log.e(TAG, "No se pudo enviar el correo del tanque: " + tanque.getNombre());
                }
            }

            return errorTemporal ? Result.retry() : Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error general en worker: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    private void inicializarFechasSiHaceFalta(TanqueAgua tanque) {
        if (tanque.getFechaInstalacion() <= 0) tanque.setFechaInstalacion(System.currentTimeMillis());
        if (tanque.getUltimoCorreoEnviado() < 0) tanque.setUltimoCorreoEnviado(0);
    }

    private boolean debeEnviarReporte(TanqueAgua tanque) {
        long ahora = System.currentTimeMillis();
        if (tanque.getFechaInstalacion() <= 0) return false;

        if (tanque.getUltimoCorreoEnviado() == 0) {
            return (ahora - tanque.getFechaInstalacion()) >= TREINTA_DIAS_MS;
        }

        return (ahora - tanque.getUltimoCorreoEnviado()) >= TREINTA_DIAS_MS;
    }

    private List<Dispositivo> obtenerHistorialDispositivo(String uid, TanqueAgua tanque) throws Exception {
        List<Dispositivo> historial = new ArrayList<>();

        String idTanque = tanque.getIdTanque();
        String idDispositivo = tanque.getIdDispositivo();

        DatabaseReference refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("tanques").child(idTanque).child("historialDispositivo");

        DataSnapshot historialSnapshot = Tasks.await(refHistorial.get());

        if (historialSnapshot.exists()) {
            for (DataSnapshot item : historialSnapshot.getChildren()) {
                Dispositivo lectura = item.getValue(Dispositivo.class);
                if (lectura != null) {
                    lectura.actualizarEstados();
                    historial.add(lectura);
                }
            }
        }

        if (historial.isEmpty() && idDispositivo != null && !idDispositivo.trim().isEmpty()) {
            DatabaseReference refDispositivo = FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).child("dispositivos").child(idDispositivo);

            DataSnapshot dispositivoSnapshot = Tasks.await(refDispositivo.get());
            Dispositivo dispositivoActual = dispositivoSnapshot.getValue(Dispositivo.class);

            if (dispositivoActual != null) {
                dispositivoActual.actualizarEstados();
                historial.add(dispositivoActual);
            }
        }

        return historial;
    }

    private String generarResumenMensual(TanqueAgua tanque, List<Dispositivo> historial) {
        StringBuilder resumen = new StringBuilder();

        resumen.append("Hola 👋\n\n");
        resumen.append("Este es el reporte mensual de tu tanque de agua en Agua Segura 💧\n\n");

        resumen.append("DATOS DEL TANQUE\n");
        resumen.append("- Nombre: ").append(obtenerNombreSeguroTanque(tanque)).append("\n");
        resumen.append("- Capacidad: ").append(tanque.getCapacidadLitros()).append(" litros\n");
        resumen.append("- Color: ").append(valorSeguro(tanque.getColor())).append("\n");
        resumen.append("- Dirección: ").append(valorSeguro(tanque.getDireccion())).append("\n");
        resumen.append("- Fecha de instalación: ").append(formatearFecha(tanque.getFechaInstalacion())).append("\n\n");

        if (historial == null || historial.isEmpty()) {
            resumen.append("ESTADO DEL DISPOSITIVO\n");
            resumen.append("- No se encontró historial de lecturas para este período.\n");
            resumen.append("- Recomendación: verificar sincronización del dispositivo.\n\n");
            resumen.append("Gracias por usar Agua Segura.\n");
            return resumen.toString();
        }

        double promedioPh            = promedioPh(historial);
        double promedioConductividad = promedioConductividad(historial);
        double promedioTurbidez      = promedioTurbidez(historial);
        double promedioUltrasonico   = promedioUltrasonico(historial);

        int alertas  = contarEstados(historial, "Alerta");
        int peligros = contarEstados(historial, "Peligro");
        String estadoGeneral = calcularEstadoGeneral(historial);

        resumen.append("RESUMEN DEL DISPOSITIVO\n");
        resumen.append("- Cantidad de registros analizados: ").append(historial.size()).append("\n");
        resumen.append("- pH promedio: ").append(formato2Decimales(promedioPh)).append("\n");
        resumen.append("- Conductividad promedio: ").append(formato2Decimales(promedioConductividad)).append("\n");
        resumen.append("- Turbidez promedio: ").append(formato2Decimales(promedioTurbidez)).append("\n");
        resumen.append("- Nivel promedio (ultrasónico): ").append(formato2Decimales(promedioUltrasonico)).append("\n");
        resumen.append("- Eventos en alerta: ").append(alertas).append("\n");
        resumen.append("- Eventos en peligro: ").append(peligros).append("\n");
        resumen.append("- Estado general del período: ").append(estadoGeneral).append("\n\n");

        Dispositivo ultimaLectura = historial.get(historial.size() - 1);

        resumen.append("ÚLTIMA LECTURA REGISTRADA\n");
        resumen.append("- Fecha: ").append(formatearFecha(ultimaLectura.getTimestamp())).append("\n");
        resumen.append("- pH: ").append(formato2Decimales(ultimaLectura.getPh())).append(" (").append(ultimaLectura.getEstadoPH()).append(")\n");
        resumen.append("- Conductividad: ").append(formato2Decimales(ultimaLectura.getConductividad())).append(" (").append(ultimaLectura.getEstadoConductividad()).append(")\n");
        resumen.append("- Turbidez: ").append(formato2Decimales(ultimaLectura.getTurbidez())).append(" (").append(ultimaLectura.getEstadoTurbidez()).append(")\n");
        resumen.append("- Nivel: ").append(formato2Decimales(ultimaLectura.getUltrasonico())).append("\n");
        resumen.append("- Estado general actual: ").append(ultimaLectura.getEstadoGeneral()).append("\n\n");

        resumen.append("Gracias por usar Agua Segura 💙\n");

        return resumen.toString();
    }

    private double promedioPh(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) suma += d.getPh();
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioConductividad(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) suma += d.getConductividad();
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioTurbidez(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) suma += d.getTurbidez();
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioUltrasonico(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) suma += d.getUltrasonico();
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private int contarEstados(List<Dispositivo> historial, String palabraClave) {
        int contador = 0;
        for (Dispositivo d : historial) {
            String estado = d.getEstadoGeneral();
            if (estado != null && estado.contains(palabraClave)) contador++;
        }
        return contador;
    }

    private String calcularEstadoGeneral(List<Dispositivo> historial) {
        if (contarEstados(historial, "Peligro") > 0) return "Peligro 🔥";
        if (contarEstados(historial, "Alerta") > 0) return "Alerta ⚠️";
        return "Normal 👍";
    }

    private boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(CORREO_REPORTE, CLAVE_APP_CORREO);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CORREO_REPORTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setText(cuerpo);

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            Log.e(TAG, "Error SMTP: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al enviar correo: " + e.getMessage(), e);
            return false;
        }
    }

    private void guardarUltimoCorreoEnviado(String uid, TanqueAgua tanque) {
        FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("tanques")
                .child(tanque.getIdTanque()).child("ultimoCorreoEnviado")
                .setValue(tanque.getUltimoCorreoEnviado());
    }

    private String obtenerNombreSeguroTanque(TanqueAgua tanque) {
        if (tanque.getNombre() == null || tanque.getNombre().trim().isEmpty()) return "Tanque sin nombre";
        return tanque.getNombre();
    }

    private String valorSeguro(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? "No disponible" : valor;
    }

    private String formatearFecha(long timestamp) {
        if (timestamp <= 0) return "No disponible";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    private String formato2Decimales(double valor) {
        return String.format(Locale.getDefault(), "%.2f", valor);
    }
}
