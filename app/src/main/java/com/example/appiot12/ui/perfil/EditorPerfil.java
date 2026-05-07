package com.example.appiot12.ui.perfil;

// =====================================================
// 📦 EDITOR DE PERFIL
// =====================================================
// Esta pantalla permite al usuario:
//
// 1. Ver su correo actual
// 2. Editar su nombre
// 3. Guardar cambios en Firebase Realtime Database
// 4. Actualizar también el displayName en Firebase Auth
// 5. Cancelar y volver sin guardar
//
// IMPORTANTE:
// - El correo se muestra, pero NO se edita aquí.
// - Cambiar correo en Firebase Auth requiere un flujo
//   más delicado, así que por ahora solo se edita nombre.
// =====================================================

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditorPerfil extends AppCompatActivity {

    // =====================================================
    // 🔐 FIREBASE
    // =====================================================
    // auth -> para obtener al usuario autenticado
    private FirebaseAuth auth;

    // =====================================================
    // 📝 CAMPOS DE LA PANTALLA
    // =====================================================
    // etNombrePerfil -> nombre editable
    // etCorreoPerfil -> correo solo lectura
    private EditText etNombrePerfil, etCorreoPerfil;

    // =====================================================
    // 🔘 BOTONES
    // =====================================================
    // btnGuardarPerfil -> guarda cambios
    // btnCancelarPerfil -> vuelve sin guardar
    private Button btnGuardarPerfil, btnCancelarPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_perfil);

        // Inicializa Firebase Auth.
        auth = FirebaseAuth.getInstance();

        // Conecta las vistas del XML con Java.
        initViews();

        // Carga los datos actuales del usuario.
        cargarDatosUsuario();

        // Configura las acciones de botones.
        initActions();
    }

    // =====================================================
    // 🔗 VINCULAR VISTAS
    // =====================================================
    // Busca en el XML los elementos que usará esta Activity.
    private void initViews() {
        etNombrePerfil = findViewById(R.id.etNombrePerfil);
        etCorreoPerfil = findViewById(R.id.etCorreoPerfil);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);
        btnCancelarPerfil = findViewById(R.id.btnCancelarPerfil);
    }

    // =====================================================
    // 👤 CARGAR DATOS DEL USUARIO
    // =====================================================
    // Este método:
    // 1. Verifica que exista sesión activa
    // 2. Muestra el correo desde Firebase Auth
    // 3. Busca el nombre en Realtime Database
    // 4. Si no encuentra nombre, intenta usar displayName
    private void cargarDatosUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        // Si no hay sesión, no tiene sentido seguir aquí.
        if (user == null) {
            toast("No hay sesión activa");
            finish();
            return;
        }

        // Muestra el correo autenticado.
        etCorreoPerfil.setText(safe(user.getEmail()));

        // Busca el nombre guardado en la base de datos.
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid())
                .child("nombre")
                .get()
                .addOnSuccessListener(snapshot -> cargarNombre(snapshot, user))
                .addOnFailureListener(e -> {
                    // Si falla la lectura, al menos intenta mostrar displayName.
                    etNombrePerfil.setText(safe(user.getDisplayName()));
                    toast("No se pudo cargar el nombre desde la base de datos");
                });
    }

    // =====================================================
    // 🧠 CARGAR NOMBRE
    // =====================================================
    // Si existe nombre en la base, se usa ese.
    // Si no, se usa el displayName de Firebase Auth.
    private void cargarNombre(DataSnapshot snapshot, FirebaseUser user) {
        String nombreDB = snapshot.getValue(String.class);

        if (nombreDB != null && !nombreDB.trim().isEmpty()) {
            etNombrePerfil.setText(nombreDB.trim());
        } else {
            etNombrePerfil.setText(safe(user.getDisplayName()));
        }
    }

    // =====================================================
    // ⚙️ CONFIGURAR ACCIONES
    // =====================================================
    // Define qué hace cada botón.
    private void initActions() {

        // Guarda el nuevo nombre.
        btnGuardarPerfil.setOnClickListener(v -> guardarCambios());

        // Cierra la pantalla sin guardar.
        btnCancelarPerfil.setOnClickListener(v -> finish());
    }

    // =====================================================
    // 💾 GUARDAR CAMBIOS
    // =====================================================
    // Este método:
    // 1. Verifica que exista usuario autenticado
    // 2. Valida que el nombre no esté vacío
    // 3. Guarda el nombre en Realtime Database
    // 4. Actualiza displayName en Firebase Auth
    private void guardarCambios() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            toast("No hay sesión activa");
            return;
        }

        // Obtiene el nombre escrito por el usuario.
        String nombre = etNombrePerfil.getText().toString().trim();

        // Validación básica.
        if (nombre.isEmpty()) {
            etNombrePerfil.setError("Ingrese su nombre");
            etNombrePerfil.requestFocus();
            return;
        }

        // Evita toques repetidos mientras se guarda.
        bloquearBotones(false);

        // Prepara datos a guardar en la base.
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("ultimaActualizacion", System.currentTimeMillis());

        // Guarda en Realtime Database.
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid())
                .updateChildren(datos)
                .addOnCompleteListener(task -> {

                    // Si falló la base, se reactivan botones y se avisa.
                    if (!task.isSuccessful()) {
                        bloquearBotones(true);
                        toast("No se pudo guardar el nombre");
                        return;
                    }

                    // Si la base salió bien, ahora se actualiza Firebase Auth.
                    actualizarDisplayName(user, nombre);
                });
    }

    // =====================================================
    // 🔄 ACTUALIZAR DISPLAY NAME
    // =====================================================
    // Esto mantiene sincronizado el nombre tanto en Auth
    // como en Realtime Database.
    private void actualizarDisplayName(FirebaseUser user, String nombre) {
        UserProfileChangeRequest profile =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build();

        user.updateProfile(profile).addOnCompleteListener(task -> {
            bloquearBotones(true);

            if (task.isSuccessful()) {
                toast("Perfil actualizado correctamente");
                finish();
            } else {
                toast("Se guardó el nombre, pero no se actualizó el perfil de autenticación");
            }
        });
    }

    // =====================================================
    // 🔒 BLOQUEAR / DESBLOQUEAR BOTONES
    // =====================================================
    // Se usa durante el guardado para evitar múltiples clics.
    private void bloquearBotones(boolean activar) {
        btnGuardarPerfil.setEnabled(activar);
        btnCancelarPerfil.setEnabled(activar);
    }

    // =====================================================
    // 🧹 EVITAR NULL
    // =====================================================
    // Evita mostrar null en los EditText.
    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =====================================================
    // 🍞 TOAST RÁPIDO
    // =====================================================
    // Muestra mensajes cortos al usuario.
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
