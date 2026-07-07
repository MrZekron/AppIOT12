package com.example.appiot12.ui.menu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.appiot12.R;
import com.example.appiot12.service.AlertaService;
import com.example.appiot12.worker.AlertaWorker;
import com.example.appiot12.ui.pago.CentroPagos;
import com.example.appiot12.ui.pago.ComprarDispositivo;
import com.example.appiot12.ui.pago.HistorialCompra;
import com.example.appiot12.ui.perfil.Configuracion;
import com.example.appiot12.ui.tanque.Agregar;
import com.example.appiot12.ui.tanque.AsociarDispositivoATanque;
import com.example.appiot12.ui.tanque.Lista;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.appiot12.ui.BaseActivity;

import java.util.concurrent.TimeUnit;

public class Menu extends BaseActivity {

    private FirebaseAuth auth;
    private TextView tvCorreoUsuario;

    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) correrAlertaInmediata();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cliente_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        mostrarCorreoUsuario();
        pedirPermisoNotificaciones();
        programarAlertaWorker();
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                correrAlertaInmediata();
            } else {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            correrAlertaInmediata();
        }
    }

    private void correrAlertaInmediata() {
        AlertaService.limpiarCooldown(this);
        OneTimeWorkRequest check = new OneTimeWorkRequest.Builder(AlertaWorker.class).build();
        WorkManager.getInstance(this).enqueueUniqueWork(
                "alerta_inmediata",
                ExistingWorkPolicy.REPLACE,
                check);
    }

    private void programarAlertaWorker() {
        PeriodicWorkRequest alertaWork = new PeriodicWorkRequest.Builder(
                AlertaWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "alerta_sensores",
                ExistingPeriodicWorkPolicy.KEEP,
                alertaWork);
    }

    private void mostrarCorreoUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvCorreoUsuario.setText("Sin sesión activa");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String correo = user.getEmail();
        tvCorreoUsuario.setText((correo != null && !correo.isEmpty()) ? correo : "Sesión activa");
    }

    private void irA(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    public void abrirConfiguracion(View v) { irA(Configuracion.class); }
    public void agregar(View v)            { irA(Agregar.class); }
    public void lista(View v)              { irA(Lista.class); }
    public void pagos(View v)              { irA(HistorialCompra.class); }
    public void comprarDispositivo(View v) { irA(ComprarDispositivo.class); }
    public void asociarDispositivo(View v) { irA(AsociarDispositivoATanque.class); }
    public void centroPagos(View v)        { irA(CentroPagos.class); }

    public void salir(View v) {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
