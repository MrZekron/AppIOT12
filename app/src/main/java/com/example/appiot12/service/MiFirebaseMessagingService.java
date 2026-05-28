package com.example.appiot12.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.menu.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "agua_alertas";
    private static final String CHANNEL_NAME = "Alertas Calidad del Agua";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        guardarToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String titulo = "Agua Segura";
        String cuerpo = "Alerta de calidad del agua";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null)
                titulo = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null)
                cuerpo = message.getNotification().getBody();
        } else if (!message.getData().isEmpty()) {
            if (message.getData().containsKey("title"))
                titulo = message.getData().get("title");
            if (message.getData().containsKey("body"))
                cuerpo = message.getData().get("body");
        }

        mostrarNotificacion(titulo, cuerpo);
    }

    private void guardarToken(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("fcmToken")
                .setValue(token);
    }

    private void mostrarNotificacion(String titulo, String cuerpo) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones cuando el agua supera umbrales seguros");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cuerpo))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
