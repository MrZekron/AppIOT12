package com.example.appiot12.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.example.appiot12.ui.admin.HistorialAcciones;
import com.example.appiot12.ui.menu.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Configuracion extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView tvCorreoConfig, tvUidConfig;
    private Button btnEditarPerfil, btnHistorial, btnEliminarCuenta, btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        auth = FirebaseAuth.getInstance();

        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);
        tvUidConfig = findViewById(R.id.tvUidConfig);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        mostrarUsuario();

        btnEditarPerfil.setOnClickListener(v -> startActivity(new Intent(this, EditorPerfil.class)));
        btnHistorial.setOnClickListener(v -> startActivity(new Intent(this, HistorialAcciones.class)));
        btnEliminarCuenta.setOnClickListener(v -> confirmarEliminacion());
        btnVolverMenu.setOnClickListener(v -> finish());
    }

    private void mostrarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            toast("No hay sesión activa");
            finish();
            return;
        }
        tvCorreoConfig.setText("Correo: " + safe(user.getEmail()));
        tvUidConfig.setText("UID: " + user.getUid());
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Solicitar eliminación")
                .setMessage("Tu cuenta quedará inhabilitada y será eliminada en 1 mes. ¿Deseas continuar?")
                .setPositiveButton("Sí, continuar", (dialog, which) -> inhabilitarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void inhabilitarCuenta() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            toast("No hay sesión activa");
            return;
        }

        bloquearBotones(false);

        long ahora = System.currentTimeMillis();
        Map<String, Object> datos = new HashMap<>();
        datos.put("bloqueado", true);
        datos.put("estado", "pendiente_eliminacion");
        datos.put("eliminacionSolicitadaAt", ahora);
        datos.put("eliminarDespuesDe", ahora + 30L * 24 * 60 * 60 * 1000);
        datos.put("ultimaActualizacion", ahora);

        FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid())
                .updateChildren(datos)
                .addOnCompleteListener(task -> {
                    bloquearBotones(true);
                    if (task.isSuccessful()) {
                        auth.signOut();
                        toast("Cuenta inhabilitada. Será eliminada en 1 mes.");
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        toast("No se pudo actualizar el estado de la cuenta");
                    }
                });
    }

    private void bloquearBotones(boolean activar) {
        btnEditarPerfil.setEnabled(activar);
        btnHistorial.setEnabled(activar);
        btnEliminarCuenta.setEnabled(activar);
        btnVolverMenu.setEnabled(activar);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
