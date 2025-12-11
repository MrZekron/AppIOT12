package com.example.appiot12;
// Paquete central del ecosistema AguaSegura. Gobernanza arquitectónica 🌊🏢

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
// Importamos widgets esenciales para UX. La “UI operativa” en acción 🖥️✨

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Herramientas modernas de UI para adaptar vistas al tamaño real del dispositivo 📱🛠️

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// Firebase como columna vertebral del backend. Datos frescos directo desde la nube 🚀🔥

import java.util.ArrayList;
// Colecciones para manejar listas dinámicas de tanques y dispositivos 📦

public class AsociarDispositivoATanque extends AppCompatActivity {
    // Activity especializada: aquí se definen relaciones IoT → Tanques
    // La sala de emparejamiento tecnológico entre hardware e infraestructura 💼🤖💧

    private Spinner spnTanques, spnDispositivos; // Dropdowns corporativos para elegir activo y dispositivo 🔽
    private Button btnAsociar;                   // Botón decisor estratégico 🟦

    private ArrayList<TanqueAgua> tanquesDisponibles = new ArrayList<>();
    // Lista dinámica de tanques del usuario 🛢️

    private ArrayList<Dispositivo> dispositivosLibres = new ArrayList<>();
    // Lista de dispositivos IoT no asignados. El “stock tecnológico disponible” 📡📦

    private ArrayAdapter<String> adapterTanques;
    private ArrayAdapter<String> adapterDispositivos;
    // Adaptadores para alimentar los Spinners con texto 🔤

    private String uid;            // UID del usuario autenticado 🔑
    private DatabaseReference refUser; // Referencia al nodo del usuario en Firebase 🗄️

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Arranca el ciclo de vida Activity 🚀
        EdgeToEdge.enable(this);           // Ajusta UI al borde completo del dispositivo 🖼️
        setContentView(R.layout.activity_asociar_dispositivo_atanque); // Pintamos layout principal 🎨

        // Configurador universal de paddings automáticos según barras del sistema 🪟
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Obtenemos UID del usuario.
        // Token supremo que define la bóveda de datos personalizada 🔐😎

        refUser = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
        // Apuntamos directo al nodo del usuario en Firebase → su espacio exclusivo en la nube 🌩️

        // Enlazamos UI con elementos del XML
        spnTanques = findViewById(R.id.spnTanques);
        spnDispositivos = findViewById(R.id.spnDispositivos);
        btnAsociar = findViewById(R.id.btnAsociar);

        cargarTanques();       // Obtenemos tanques desde Firebase 🛢️⬇️
        cargarDispositivos();  // Obtenemos dispositivos disponibles 📡⬇️

        btnAsociar.setOnClickListener(v -> asociar());
        // Acción ejecutiva del botón principal: emparejar tanque ↔ dispositivo 🤝
    }

    // =======================================================
    //   CARGAR LISTA DE TANQUES DEL USUARIO
    // =======================================================
    private void cargarTanques() {
        refUser.child("tanques").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tanquesDisponibles.clear(); // Reiniciamos lista antes de recargar ♻️
                ArrayList<String> nombres = new ArrayList<>(); // Lista de nombres para el Spinner 📋

                for (DataSnapshot s : snapshot.getChildren()) {
                    TanqueAgua t = s.getValue(TanqueAgua.class); // Convertimos snapshot → objeto TanqueAgua 🛢️

                    if (t != null) {
                        // Seguridad: Firebase a veces no trae ID, lo recuperamos de la key 🔧
                        if (t.getIdTanque() == null) t.setIdTanque(s.getKey());

                        tanquesDisponibles.add(t);      // Agregamos tanque a memoria 📥
                        nombres.add(t.getNombre());     // Mostraremos solo nombre en el Spinner 🏷️
                    }
                }

                if (nombres.isEmpty()) nombres.add("No hay tanques disponibles 😢");
                // Caso de usuario sin tanques — mensaje elegante 🪣🚫

                adapterTanques = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, nombres);
                adapterTanques.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spnTanques.setAdapter(adapterTanques); // Cargamos el Spinner con la data lista 🛠️
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error silencioso — idealmente lo logearíamos 🔥🐛
            }
        });
    }

    // =======================================================
    //   CARGAR DISPOSITIVOS NO ASOCIADOS A NINGÚN TANQUE
    // =======================================================
    private void cargarDispositivos() {
        refUser.child("dispositivos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                dispositivosLibres.clear(); // Reinicio de lista 📄
                ArrayList<String> ids = new ArrayList<>(); // IDs visibles del Spinner

                for (DataSnapshot s : snapshot.getChildren()) {

                    Dispositivo d = s.getValue(Dispositivo.class); // Snapshot → objeto IoT 🤖
                    String idTanque = s.child("idTanque").getValue(String.class);
                    // Revisamos si este dispositivo ya está asociado a un tanque 🏷️

                    if (d != null && (idTanque == null || idTanque.isEmpty())) {
                        // Solo agregamos dispositivos sin dueño 🏠❌
                        dispositivosLibres.add(d);
                        ids.add("ID: " + d.getId()); // Lo mostramos bonito en el Spinner 🎨
                    }
                }

                if (ids.isEmpty()) ids.add("No hay dispositivos disponibles 😢");
                // Mensaje corporativo para falta de inventario 📉

                adapterDispositivos = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, ids);
                adapterDispositivos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spnDispositivos.setAdapter(adapterDispositivos); // Aplicamos adaptador al Spinner 📊
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // =======================================================
    //   ASOCIAR DISPOSITIVO A TANQUE (ACCIÓN CRÍTICA)
    // =======================================================
    private void asociar() {

        if (tanquesDisponibles.isEmpty() || dispositivosLibres.isEmpty()) {
            // No hay material operativo para emparejar 🏭❌
            Toast.makeText(this, "No hay tanques o dispositivos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        int posTanque = spnTanques.getSelectedItemPosition();  // Índice seleccionado 🧮
        int posDisp = spnDispositivos.getSelectedItemPosition();

        TanqueAgua tanque = tanquesDisponibles.get(posTanque);        // Obtenemos tanque elegido
        Dispositivo dispositivo = dispositivosLibres.get(posDisp);    // Obtenemos dispositivo IoT

        // === Escribimos relación TANQUE → DISPOSITIVO ===
        refUser.child("tanques")
                .child(tanque.getIdTanque())
                .child("idDispositivo")
                .setValue(dispositivo.getId());

        // === Escribimos relación DISPOSITIVO → TANQUE ===
        refUser.child("dispositivos")
                .child(dispositivo.getId())
                .child("idTanque")
                .setValue(tanque.getIdTanque());

        // Doble vía completada: relación garantizada 🔗✨

        Toast.makeText(this, "Dispositivo asociado correctamente 🤝📡", Toast.LENGTH_LONG).show();
        finish(); // Cerramos Activity: misión cumplida ✅
    }
}
