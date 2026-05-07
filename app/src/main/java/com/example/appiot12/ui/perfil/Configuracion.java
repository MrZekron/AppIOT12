package com.example.appiot12.ui.perfil;

// =====================================================
// 📦 PANTALLA DE CONFIGURACIÓN
// =====================================================
// Esta pantalla permite al usuario:
//
// 1. Ver su correo y su UID
// 2. Ir al historial de acciones
// 3. Ir a editar perfil
// 4. Solicitar eliminación de cuenta
// 5. Volver al menú anterior
//
// IMPORTANTE:
// La eliminación de cuenta ya no es inmediata.
// Ahora la cuenta queda inhabilitada por 1 mes antes
// de ser borrada definitivamente.
//
// Eso significa que al presionar "Eliminar cuenta":
// - se bloquea la cuenta
// - se marca como pendiente de eliminación
// - se guarda la fecha de solicitud
// - se guarda la fecha futura de borrado
// - se cierra la sesión del usuario
// =====================================================

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

    // =====================================================
    // 🔐 FIREBASE AUTH
    // =====================================================
    // Se usa para saber quién está logueado y cerrar sesión.
    private FirebaseAuth auth;

    // =====================================================
    // 📝 TEXTOS DE LA PANTALLA
    // =====================================================
    // Aquí mostramos datos básicos del usuario actual.
    private TextView tvCorreoConfig;
    private TextView tvUidConfig;

    // =====================================================
    // 🔘 BOTONES
    // =====================================================
    // Botones principales de la pantalla de configuración.
    private Button btnEditarPerfil;
    private Button btnHistorial;
    private Button btnEliminarCuenta;
    private Button btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Inicializa Firebase Auth.
        auth = FirebaseAuth.getInstance();

        // Conecta los elementos del XML con Java.
        initViews();

        // Muestra los datos del usuario autenticado.
        mostrarUsuario();

        // Configura la lógica de los botones.
        initActions();
    }

    // =====================================================
    // 🔗 VINCULAR VISTAS
    // =====================================================
    // Este método conecta cada variable Java con su ID del XML.
    private void initViews() {
        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);
        tvUidConfig = findViewById(R.id.tvUidConfig);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);
    }

    // =====================================================
    // 👤 MOSTRAR DATOS DEL USUARIO
    // =====================================================
    // Toma el usuario actualmente autenticado y muestra:
    // - correo
    // - UID
    //
    // Si no existe sesión activa, se cierra esta pantalla.
    private void mostrarUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            toast("No hay sesión activa");
            finish();
            return;
        }

        // Muestra el correo del usuario.
        tvCorreoConfig.setText("Correo: " + safe(user.getEmail()));

        // Muestra el identificador único del usuario en Firebase Auth.
        tvUidConfig.setText("UID: " + user.getUid());
    }

    // =====================================================
    // ⚙️ CONFIGURAR ACCIONES DE BOTONES
    // =====================================================
    // Aquí se define qué hace cada botón.
    private void initActions() {

        // Abre la pantalla de edición de perfil.
        btnEditarPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, EditorPerfil.class)));

        // Abre el historial de acciones del usuario.
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(this, HistorialAcciones.class)));

        // Muestra diálogo de confirmación antes de inhabilitar la cuenta.
        btnEliminarCuenta.setOnClickListener(v ->
                confirmarEliminacion());

        // Cierra esta pantalla y vuelve a la anterior.
        btnVolverMenu.setOnClickListener(v -> finish());
    }

    // =====================================================
    // ⚠️ CONFIRMAR ELIMINACIÓN
    // =====================================================
    // Antes de inhabilitar la cuenta, se muestra un diálogo
    // para confirmar que el usuario realmente quiere hacerlo.
    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Solicitar eliminación")
                .setMessage("Tu cuenta quedará inhabilitada y será eliminada en 1 mes. ¿Deseas continuar?")
                .setPositiveButton("Sí, continuar", (dialog, which) -> inhabilitarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // =====================================================
    // 🚫 INHABILITAR CUENTA
    // =====================================================
    // En vez de borrar la cuenta inmediatamente, se hace esto:
    //
    // 1. Se bloquea la cuenta
    // 2. Se marca el estado como pendiente de eliminación
    // 3. Se guarda cuándo se solicitó la eliminación
    // 4. Se guarda la fecha futura de borrado (30 días)
    // 5. Se cierra la sesión
    //
    // El borrado real se puede hacer más adelante con backend.
    private void inhabilitarCuenta() {
        FirebaseUser user = auth.getCurrentUser();

        // Si no hay usuario, no se puede continuar.
        if (user == null) {
            toast("No hay sesión activa");
            return;
        }

        // Desactiva botones para evitar clics repetidos.
        bloquearBotones(false);

        // Tiempo actual en milisegundos.
        long ahora = System.currentTimeMillis();

        // 30 días expresados en milisegundos.
        long unMes = 30L * 24 * 60 * 60 * 1000;

        // Fecha estimada de eliminación futura.
        long fechaBorrado = ahora + unMes;

        // Mapa con los cambios que se guardarán en Firebase.
        Map<String, Object> datos = new HashMap<>();
        datos.put("bloqueado", true);
        datos.put("estado", "pendiente_eliminacion");
        datos.put("eliminacionSolicitadaAt", ahora);
        datos.put("eliminarDespuesDe", fechaBorrado);
        datos.put("ultimaActualizacion", ahora);

        // Actualiza el nodo del usuario en Realtime Database.
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid())
                .updateChildren(datos)
                .addOnCompleteListener(task -> {

                    // Reactiva botones cuando termina el proceso.
                    bloquearBotones(true);

                    if (task.isSuccessful()) {
                        // Cierra sesión para que no siga usando la cuenta.
                        auth.signOut();

                        // Informa al usuario.
                        toast("Cuenta inhabilitada. Será eliminada en 1 mes.");

                        // Lo manda al inicio de la app.
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        toast("No se pudo actualizar el estado de la cuenta");
                    }
                });
    }

    // =====================================================
    // 🔒 BLOQUEAR / DESBLOQUEAR BOTONES
    // =====================================================
    // Este método se usa para evitar que el usuario toque
    // varias veces los botones mientras ocurre una operación.
    //
    // activar = true  -> botones activos
    // activar = false -> botones desactivados
    private void bloquearBotones(boolean activar) {
        btnEditarPerfil.setEnabled(activar);
        btnHistorial.setEnabled(activar);
        btnEliminarCuenta.setEnabled(activar);
        btnVolverMenu.setEnabled(activar);
    }

    // =====================================================
    // 🧹 EVITAR NULL
    // =====================================================
    // Si el correo viene nulo, se reemplaza por texto vacío
    // para evitar mostrar "null" en pantalla.
    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =====================================================
    // 🍞 MENSAJE RÁPIDO
    // =====================================================
    // Método de apoyo para mostrar Toasts fácilmente.
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
