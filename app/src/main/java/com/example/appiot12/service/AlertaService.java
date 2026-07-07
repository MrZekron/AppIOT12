package com.example.appiot12.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.appiot12.R;
import com.example.appiot12.model.Alerta;
import com.example.appiot12.ui.menu.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlertaService {

    private static final String CHANNEL_ID = "agua_alertas";
    private static final String CHANNEL_NAME = "Alertas Calidad del Agua";
    private static final String PREFS_NAME = "alertas_cooldown";
    private static final long COOLDOWN_MS = 60 * 60 * 1000L; // 1 hour per device+type

    public static void evaluarLectura(Context ctx, String uid, String idDispositivo,
                                       double ph, double conductividad,
                                       double turbidez, double nivelPorcentaje) {
        if (uid == null || idDispositivo == null) return;

        // pH
        if (!Double.isNaN(ph)) {
            if (ph < 6.0 || ph > 9.0) {
                double umbral = ph < 6.0 ? 6.0 : 9.0;
                dispararSiNoCooldown(ctx, uid, idDispositivo, "ph_peligro",
                        "pH fuera de rango seguro",
                        String.format("pH %.2f (umbral: %.1f)", ph, umbral),
                        ph, umbral);
            } else if (ph < 6.5 || ph > 8.5) {
                double umbral = ph < 6.5 ? 6.5 : 8.5;
                dispararSiNoCooldown(ctx, uid, idDispositivo, "ph_alerta",
                        "pH en zona de alerta",
                        String.format("pH %.2f (rango normal: 6.5-8.5)", ph),
                        ph, umbral);
            }
        }

        // Conductividad
        if (!Double.isNaN(conductividad)) {
            if (conductividad > 2500) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "conductividad_peligro",
                        "Conductividad peligrosa",
                        String.format("%.0f µS/cm (máx: 2500)", conductividad),
                        conductividad, 2500);
            } else if (conductividad > 1500) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "conductividad_alerta",
                        "Conductividad elevada",
                        String.format("%.0f µS/cm (máx normal: 1500)", conductividad),
                        conductividad, 1500);
            }
        }

        // Turbidez
        if (!Double.isNaN(turbidez)) {
            if (turbidez > 10) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "turbidez_peligro",
                        "Turbidez peligrosa",
                        String.format("%.1f NTU (máx: 10)", turbidez),
                        turbidez, 10);
            } else if (turbidez > 5) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "turbidez_alerta",
                        "Turbidez elevada",
                        String.format("%.1f NTU (máx normal: 5)", turbidez),
                        turbidez, 5);
            }
        }

        // Nivel
        if (!Double.isNaN(nivelPorcentaje)) {
            if (nivelPorcentaje < 20) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "nivel_bajo",
                        "Nivel de agua muy bajo",
                        String.format("Nivel: %.0f%% (mín: 20%%)", nivelPorcentaje),
                        nivelPorcentaje, 20);
            } else if (nivelPorcentaje < 30) {
                dispararSiNoCooldown(ctx, uid, idDispositivo, "nivel_bajo_alerta",
                        "Nivel de agua bajo",
                        String.format("Nivel: %.0f%% (mín normal: 30%%)", nivelPorcentaje),
                        nivelPorcentaje, 30);
            }
        }
    }

    private static void dispararSiNoCooldown(Context ctx, String uid, String idDispositivo,
                                              String tipo, String titulo, String detalle,
                                              double valorDetectado, double umbral) {
        String cooldownKey = idDispositivo + "_" + tipo;
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastAlert = prefs.getLong(cooldownKey, 0);
        long now = System.currentTimeMillis();

        if (now - lastAlert < COOLDOWN_MS) return;

        prefs.edit().putLong(cooldownKey, now).apply();

        guardarAlertaFirebase(uid, idDispositivo, tipo, valorDetectado, umbral);
        HistorialService.registrarEvento("alerta", titulo + " — " + detalle, idDispositivo, null);
        mostrarNotificacion(ctx, titulo, "[" + idDispositivo + "] " + detalle);
    }

    private static void guardarAlertaFirebase(String uid, String idDispositivo,
                                               String tipo, double valorDetectado, double umbral) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("alertas").child(uid);
        String id = ref.push().getKey();
        if (id == null) return;
        Alerta alerta = new Alerta(id, idDispositivo, uid, tipo, valorDetectado, umbral);
        ref.child(id).setValue(alerta);
    }

    public static void limpiarCooldown(Context ctx) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void mostrarNotificacion(Context ctx, String titulo, String cuerpo) {
        NotificationManager manager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones cuando el agua supera umbrales seguros");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cuerpo))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
