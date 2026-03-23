package com.example.appiot12; // 📦 Paquete del proyecto.

import android.content.Context; // 🏢 Contexto de la app.
import android.util.Log; // 🪵 Logs para depuración.

import androidx.annotation.NonNull; // ✅ Indica que algo no puede ser null.
import androidx.work.Worker; // 🤖 Clase base para tareas en segundo plano.
import androidx.work.WorkerParameters; // ⚙️ Parámetros del Worker.

import com.google.android.gms.tasks.Tasks; // ⏳ Permite esperar tareas async.
import com.google.firebase.auth.FirebaseAuth; // 👤 Firebase Auth.
import com.google.firebase.auth.FirebaseUser; // 🙋 Usuario autenticado.
import com.google.firebase.database.DataSnapshot; // 📦 Snapshot de Firebase.
import com.google.firebase.database.DatabaseReference; // 📡 Referencia a Firebase.
import com.google.firebase.database.FirebaseDatabase; // ☁️ Base de datos.

import java.text.SimpleDateFormat; // 📅 Formato de fechas.
import java.util.ArrayList; // 📋 Lista dinámica.
import java.util.Date; // 🕒 Fecha.
import java.util.List; // 📚 Interfaz lista.
import java.util.Locale; // 🌍 Configuración regional.
import java.util.Properties; // ⚙️ Propiedades SMTP.

import javax.mail.Authenticator; // 🔐 Autenticador SMTP.
import javax.mail.Message; // 📨 Mensaje de correo.
import javax.mail.MessagingException; // ❌ Excepción de correo.
import javax.mail.PasswordAuthentication; // 🔑 Credenciales SMTP.
import javax.mail.Session; // 📬 Sesión SMTP.
import javax.mail.Transport; // 🚚 Envío del mensaje.
import javax.mail.internet.InternetAddress; // 🌐 Dirección de correo.
import javax.mail.internet.MimeMessage; // ✉️ Mensaje MIME.

/**
 * 🤖 Worker encargado de revisar tanques y enviar reportes mensuales por correo.
 *
 * ⚠️ Esta es una solución temporal desde Android.
 * La solución final será con Firebase Functions.
 */
public class ReporteMensualWorker extends Worker {

    private static final String TAG = "ReporteMensualWorker"; // 🏷️ Tag de logs.
    private static final long TREINTA_DIAS_MS = 30L * 24L * 60L * 60L * 1000L; // 📆 30 días.

    // ⚠️ Reemplaza estos datos por tu correo real y tu App Password de Gmail.
    private static final String CORREO_REPORTE = "TU_CORREO@gmail.com"; // 📧 Correo emisor.
    private static final String CLAVE_APP_CORREO = "TU_APP_PASSWORD"; // 🔑 App Password.

    public ReporteMensualWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams); // 🔗 Llama al constructor padre.
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "🚀 Iniciando ReporteMensualWorker...");

        try {
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser(); // 👤 Usuario actual.

            if (usuario == null) {
                Log.e(TAG, "❌ No hay usuario autenticado.");
                return Result.retry();
            }

            String uid = usuario.getUid(); // 🆔 UID usuario.
            String emailDestino = usuario.getEmail(); // 📧 Email del usuario.

            if (emailDestino == null || emailDestino.trim().isEmpty()) {
                Log.e(TAG, "❌ El usuario no tiene correo válido.");
                return Result.failure();
            }

            DatabaseReference refTanques = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("tanques");

            DataSnapshot snapshotTanques = Tasks.await(refTanques.get()); // ⏳ Leer tanques.

            if (!snapshotTanques.exists()) {
                Log.d(TAG, "📭 No hay tanques para procesar.");
                return Result.success();
            }

            boolean errorTemporal = false; // 🚨 Control de reintento.

            for (DataSnapshot tanqueSnapshot : snapshotTanques.getChildren()) {
                TanqueAgua tanque = tanqueSnapshot.getValue(TanqueAgua.class); // 💧 Mapear tanque.

                if (tanque == null) {
                    continue; // ⏭️ Saltar si vino mal.
                }

                if (tanque.getIdTanque() == null || tanque.getIdTanque().trim().isEmpty()) {
                    tanque.setIdTanque(tanqueSnapshot.getKey()); // 🆔 Fallback con key Firebase.
                }

                inicializarFechasSiHaceFalta(tanque); // 🕒 Completar fechas antiguas.

                if (!debeEnviarReporte(tanque)) {
                    continue; // ⏭️ Si no toca reporte, seguir.
                }

                List<Dispositivo> historial = obtenerHistorialDispositivo(uid, tanque); // 📡 Historial.
                String resumen = generarResumenMensual(tanque, historial); // 📊 Texto correo.
                String asunto = "Reporte mensual de tu tanque: " + obtenerNombreSeguroTanque(tanque); // 📨 Asunto.

                boolean enviado = enviarCorreo(emailDestino, asunto, resumen); // 📧 Enviar.

                if (enviado) {
                    tanque.marcarCorreoEnviado(); // ✅ Marcar envío.
                    guardarUltimoCorreoEnviado(uid, tanque); // 💾 Guardar en Firebase.
                    Log.d(TAG, "✅ Correo enviado para tanque: " + tanque.getNombre());
                } else {
                    errorTemporal = true; // ❌ Falló al menos uno.
                    Log.e(TAG, "❌ No se pudo enviar el correo del tanque: " + tanque.getNombre());
                }
            }

            return errorTemporal ? Result.retry() : Result.success(); // 🔄 o ✅

        } catch (Exception e) {
            Log.e(TAG, "🔥 Error general en worker: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    // =========================
    // 📆 FECHAS
    // =========================
    private void inicializarFechasSiHaceFalta(TanqueAgua tanque) {
        if (tanque.getFechaCreacion() <= 0) {
            tanque.setFechaCreacion(System.currentTimeMillis()); // 🕒 Si no tenía fecha, usar ahora.
        }

        if (tanque.getUltimoCorreoEnviado() < 0) {
            tanque.setUltimoCorreoEnviado(0); // 🔧 Corregir valor inválido.
        }
    }

    private boolean debeEnviarReporte(TanqueAgua tanque) {
        long ahora = System.currentTimeMillis(); // 🕒 Hora actual.

        if (tanque.getFechaCreacion() <= 0) {
            return false;
        }

        if (tanque.getUltimoCorreoEnviado() == 0) {
            return (ahora - tanque.getFechaCreacion()) >= TREINTA_DIAS_MS; // 📆 Desde creación.
        }

        return (ahora - tanque.getUltimoCorreoEnviado()) >= TREINTA_DIAS_MS; // 📆 Desde último envío.
    }

    // =========================
    // 📡 HISTORIAL
    // =========================
    private List<Dispositivo> obtenerHistorialDispositivo(String uid, TanqueAgua tanque) throws Exception {
        List<Dispositivo> historial = new ArrayList<>(); // 📋 Lista de lecturas.

        String idTanque = tanque.getIdTanque(); // 🆔 ID tanque.
        String idDispositivo = tanque.getIdDispositivo(); // 📡 ID dispositivo.

        DatabaseReference refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque)
                .child("historialDispositivo");

        DataSnapshot historialSnapshot = Tasks.await(refHistorial.get()); // ⏳ Leer historial.

        if (historialSnapshot.exists()) {
            for (DataSnapshot item : historialSnapshot.getChildren()) {
                Dispositivo lectura = item.getValue(Dispositivo.class); // 📡 Convertir lectura.
                if (lectura != null) {
                    lectura.actualizarEstados(); // 🔄 Recalcular estados.
                    historial.add(lectura); // ➕ Agregar.
                }
            }
        }

        if (historial.isEmpty() && idDispositivo != null && !idDispositivo.trim().isEmpty()) {
            DatabaseReference refDispositivo = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("dispositivos")
                    .child(idDispositivo);

            DataSnapshot dispositivoSnapshot = Tasks.await(refDispositivo.get()); // ⏳ Leer dispositivo.

            Dispositivo dispositivoActual = dispositivoSnapshot.getValue(Dispositivo.class);

            if (dispositivoActual != null) {
                dispositivoActual.actualizarEstados(); // 🔄 Recalcular estados.
                historial.add(dispositivoActual); // ➕ Usarlo como mínimo histórico.
            }
        }

        return historial; // ✅ Retornar datos.
    }

    // =========================
    // 📊 RESUMEN
    // =========================
    private String generarResumenMensual(TanqueAgua tanque, List<Dispositivo> historial) {
        StringBuilder resumen = new StringBuilder(); // 🧱 Constructor de texto.

        resumen.append("Hola 👋\n\n");
        resumen.append("Este es el reporte mensual de tu tanque de agua en Agua Segura 💧\n\n");

        resumen.append("📦 DATOS DEL TANQUE\n");
        resumen.append("- Nombre: ").append(obtenerNombreSeguroTanque(tanque)).append("\n");
        resumen.append("- Capacidad: ").append(valorSeguro(tanque.getCapacidad())).append(" litros\n");
        resumen.append("- Color: ").append(valorSeguro(tanque.getColor())).append("\n");
        resumen.append("- Dirección: ").append(valorSeguro(tanque.getDireccion())).append("\n");
        resumen.append("- Fecha de creación: ").append(formatearFecha(tanque.getFechaCreacion())).append("\n\n");

        if (historial == null || historial.isEmpty()) {
            resumen.append("📡 ESTADO DEL DISPOSITIVO\n");
            resumen.append("- No se encontró historial de lecturas para este período.\n");
            resumen.append("- Recomendación: verificar sincronización del dispositivo.\n\n");
            resumen.append("Gracias por usar Agua Segura 🚀\n");
            return resumen.toString();
        }

        double promedioPh = promedioPh(historial); // ⚗️
        double promedioConductividad = promedioConductividad(historial); // ⚡
        double promedioTurbidez = promedioTurbidez(historial); // 🌫
        double promedioUltrasonico = promedioUltrasonico(historial); // 📏

        int alertas = contarEstados(historial, "Alerta"); // ⚠️
        int peligros = contarEstados(historial, "Peligro"); // 🔥
        String estadoGeneral = calcularEstadoGeneral(historial); // 📊

        resumen.append("📡 RESUMEN DEL DISPOSITIVO\n");
        resumen.append("- Cantidad de registros analizados: ").append(historial.size()).append("\n");
        resumen.append("- pH promedio: ").append(formato2Decimales(promedioPh)).append("\n");
        resumen.append("- Conductividad promedio: ").append(formato2Decimales(promedioConductividad)).append("\n");
        resumen.append("- Turbidez promedio: ").append(formato2Decimales(promedioTurbidez)).append("\n");
        resumen.append("- Nivel promedio (ultrasónico): ").append(formato2Decimales(promedioUltrasonico)).append("\n");
        resumen.append("- Eventos en alerta: ").append(alertas).append("\n");
        resumen.append("- Eventos en peligro: ").append(peligros).append("\n");
        resumen.append("- Estado general del período: ").append(estadoGeneral).append("\n\n");

        Dispositivo ultimaLectura = historial.get(historial.size() - 1); // 🕒 Última lectura.

        resumen.append("🧾 ÚLTIMA LECTURA REGISTRADA\n");
        resumen.append("- Fecha: ").append(formatearFecha(ultimaLectura.getTimestamp())).append("\n");
        resumen.append("- pH: ").append(formato2Decimales(ultimaLectura.getPh())).append(" (").append(ultimaLectura.getEstadoPH()).append(")\n");
        resumen.append("- Conductividad: ").append(formato2Decimales(ultimaLectura.getConductividad())).append(" (").append(ultimaLectura.getEstadoConductividad()).append(")\n");
        resumen.append("- Turbidez: ").append(formato2Decimales(ultimaLectura.getTurbidez())).append(" (").append(ultimaLectura.getEstadoTurbidez()).append(")\n");
        resumen.append("- Nivel: ").append(formato2Decimales(ultimaLectura.getUltrasonico())).append("\n");
        resumen.append("- Estado general actual: ").append(ultimaLectura.getEstadoGeneral()).append("\n\n");

        resumen.append("Gracias por usar Agua Segura 💙\n");
        resumen.append("Seguimos monitoreando la calidad del agua de tu tanque 🌊\n");

        return resumen.toString();
    }

    // =========================
    // 📈 CÁLCULOS
    // =========================
    private double promedioPh(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) {
            suma += d.getPh();
        }
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioConductividad(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) {
            suma += d.getConductividad();
        }
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioTurbidez(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) {
            suma += d.getTurbidez();
        }
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private double promedioUltrasonico(List<Dispositivo> historial) {
        double suma = 0;
        for (Dispositivo d : historial) {
            suma += d.getUltrasonico();
        }
        return historial.isEmpty() ? 0 : suma / historial.size();
    }

    private int contarEstados(List<Dispositivo> historial, String palabraClave) {
        int contador = 0;
        for (Dispositivo d : historial) {
            String estado = d.getEstadoGeneral();
            if (estado != null && estado.contains(palabraClave)) {
                contador++;
            }
        }
        return contador;
    }

    private String calcularEstadoGeneral(List<Dispositivo> historial) {
        int peligros = contarEstados(historial, "Peligro");
        int alertas = contarEstados(historial, "Alerta");

        if (peligros > 0) return "Peligro 🔥";
        if (alertas > 0) return "Alerta ⚠️";
        return "Normal 👍";
    }

    // =========================
    // 📧 ENVÍO SMTP
    // =========================
    private boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            Properties props = new Properties(); // ⚙️ Config SMTP.
            props.put("mail.smtp.auth", "true"); // 🔐 Auth.
            props.put("mail.smtp.starttls.enable", "true"); // 🔒 TLS.
            props.put("mail.smtp.host", "smtp.gmail.com"); // 🌐 Host.
            props.put("mail.smtp.port", "587"); // 🚪 Puerto.

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(CORREO_REPORTE, CLAVE_APP_CORREO); // 🔑 Credenciales.
                }
            });

            Message message = new MimeMessage(session); // 📨 Crear mensaje.
            message.setFrom(new InternetAddress(CORREO_REPORTE)); // 📤 Remitente.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario)); // 📥 Destino.
            message.setSubject(asunto); // 🏷️ Asunto.
            message.setText(cuerpo); // 📝 Cuerpo.

            Transport.send(message); // 🚚 Enviar.

            return true; // ✅ Éxito.

        } catch (MessagingException e) {
            Log.e(TAG, "❌ Error SMTP: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "🔥 Error general al enviar correo: " + e.getMessage(), e);
            return false;
        }
    }

    // =========================
    // 💾 GUARDAR FECHA DE ENVÍO
    // =========================
    private void guardarUltimoCorreoEnviado(String uid, TanqueAgua tanque) {
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanque.getIdTanque())
                .child("ultimoCorreoEnviado")
                .setValue(tanque.getUltimoCorreoEnviado()); // 💾 Guardar timestamp.
    }

    // =========================
    // 🧰 HELPERS
    // =========================
    private String obtenerNombreSeguroTanque(TanqueAgua tanque) {
        if (tanque.getNombre() == null || tanque.getNombre().trim().isEmpty()) {
            return "Tanque sin nombre";
        }
        return tanque.getNombre();
    }

    private String valorSeguro(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "No disponible";
        }
        return valor;
    }

    private String formatearFecha(long timestamp) {
        if (timestamp <= 0) {
            return "No disponible";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }

    private String formato2Decimales(double valor) {
        return String.format(Locale.getDefault(), "%.2f", valor);
    }
}