package com.example.appiot12;
// 📦 Paquete central del proyecto Agua Segura.
// Aquí se coordinan acciones importantes entre tanques y dispositivos 🏢💧🤖

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
// 🖥️ Componentes visuales básicos para interactuar con el usuario

import androidx.appcompat.app.AppCompatActivity;
// 🎖️ Activity base moderna y estable

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ☁️ Firebase: donde viven nuestros datos en la nube

import java.util.ArrayList;
import java.util.List;
// 📦 Listas dinámicas para manejar tanques y dispositivos

/**
 * 🔗 AsociarDispositivoATanque
 *
 * Esta pantalla sirve para:
 * 👉 Elegir un tanque
 * 👉 Elegir un dispositivo IoT libre
 * 👉 Asociarlos entre sí
 *
 * En simple:
 * Es como decirle al dispositivo: “tú ahora vives en ESTE tanque” 🏠💧📡
 */
public class AsociarDispositivoATanque extends AppCompatActivity {

    // 🔽 Spinners para seleccionar tanque y dispositivo
    private Spinner spnTanques;
    private Spinner spnDispositivos;

    // 🟦 Botón principal para ejecutar la asociación
    private Button btnAsociar;

    // 🛢️ Lista de tanques del usuario
    private final List<TanqueAgua> tanquesDisponibles = new ArrayList<>();

    // 📡 Lista de dispositivos que NO están asociados
    private final List<Dispositivo> dispositivosLibres = new ArrayList<>();

    // 🔤 Adaptadores para mostrar texto en los Spinners
    private ArrayAdapter<String> adapterTanques;
    private ArrayAdapter<String> adapterDispositivos;

    // 🔑 UID del usuario autenticado
    private String uid;

    // 🗄️ Referencia al nodo del usuario en Firebase
    private DatabaseReference refUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asociar_dispositivo_atanque); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos los elementos del XML
        inicializarVistas();

        // 👤 Obtenemos el UID del usuario
        uid = obtenerUidUsuario();

        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ☁️ Apuntamos al espacio del usuario en Firebase
        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        // 📥 Cargamos datos desde Firebase
        cargarTanques();
        cargarDispositivos();

        // 🤝 Acción principal: asociar tanque con dispositivo
        btnAsociar.setOnClickListener(v -> asociar());
    }

    /**
     * 🔗 Conecta los componentes visuales con el XML
     */
    private void inicializarVistas() {
        spnTanques = findViewById(R.id.spnTanques);
        spnDispositivos = findViewById(R.id.spnDispositivos);
        btnAsociar = findViewById(R.id.btnAsociar);
    }

    /**
     * 👤 Obtiene el UID del usuario logueado
     */
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 🛢️ CARGAR TANQUES DEL USUARIO
    // =====================================================
    private void cargarTanques() {

        refUsuario.child("tanques")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        tanquesDisponibles.clear();
                        List<String> nombres = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {

                            TanqueAgua tanque = s.getValue(TanqueAgua.class);

                            if (tanque != null) {
                                // 🔧 Aseguramos el ID del tanque
                                if (tanque.getIdTanque() == null) {
                                    tanque.setIdTanque(s.getKey());
                                }

                                tanquesDisponibles.add(tanque);
                                nombres.add(tanque.getNombre());
                            }
                        }

                        // 🪣 Si no hay tanques, mostramos mensaje
                        if (nombres.isEmpty()) {
                            nombres.add("No hay tanques disponibles 😢");
                        }

                        adapterTanques = new ArrayAdapter<>(
                                AsociarDispositivoATanque.this,
                                android.R.layout.simple_spinner_item,
                                nombres
                        );

                        adapterTanques.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spnTanques.setAdapter(adapterTanques);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                AsociarDispositivoATanque.this,
                                "Error al cargar tanques ⚠️",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =====================================================
    // 📡 CARGAR DISPOSITIVOS LIBRES
    // =====================================================
    private void cargarDispositivos() {

        refUsuario.child("dispositivos")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        dispositivosLibres.clear();
                        List<String> textos = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {

                            Dispositivo dispositivo = s.getValue(Dispositivo.class);
                            String idTanque = s.child("idTanque").getValue(String.class);

                            // 🏠 Solo dispositivos SIN tanque
                            if (dispositivo != null &&
                                    (idTanque == null || idTanque.isEmpty())) {

                                dispositivosLibres.add(dispositivo);
                                textos.add("ID: " + dispositivo.getId());
                            }
                        }

                        if (textos.isEmpty()) {
                            textos.add("No hay dispositivos disponibles 😢");
                        }

                        adapterDispositivos = new ArrayAdapter<>(
                                AsociarDispositivoATanque.this,
                                android.R.layout.simple_spinner_item,
                                textos
                        );

                        adapterDispositivos.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spnDispositivos.setAdapter(adapterDispositivos);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                AsociarDispositivoATanque.this,
                                "Error al cargar dispositivos ⚠️",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =====================================================
    // 🤝 ASOCIAR TANQUE ↔ DISPOSITIVO
    // =====================================================
    private void asociar() {

        // 🛑 Validación básica
        if (tanquesDisponibles.isEmpty() || dispositivosLibres.isEmpty()) {
            Toast.makeText(
                    this,
                    "No hay tanques o dispositivos disponibles",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int posTanque = spnTanques.getSelectedItemPosition();
        int posDispositivo = spnDispositivos.getSelectedItemPosition();

        TanqueAgua tanque = tanquesDisponibles.get(posTanque);
        Dispositivo dispositivo = dispositivosLibres.get(posDispositivo);

        // 🔗 Guardamos relación TANQUE → DISPOSITIVO
        refUsuario.child("tanques")
                .child(tanque.getIdTanque())
                .child("idDispositivo")
                .setValue(dispositivo.getId());

        // 🔗 Guardamos relación DISPOSITIVO → TANQUE
        refUsuario.child("dispositivos")
                .child(dispositivo.getId())
                .child("idTanque")
                .setValue(tanque.getIdTanque());

        // ✅ Confirmación al usuario
        Toast.makeText(
                this,
                "Dispositivo asociado correctamente 🤝📡",
                Toast.LENGTH_LONG
        ).show();

        finish(); // 🚪 Cerramos pantalla
    }
}
