package com.example.appiot12; // 📦 Este archivo pertenece al paquete principal de la app

import android.content.Intent; // 🚪 Para cambiar a otra pantalla (Activity)
import android.os.Bundle; // 🎒 Información de estado al crear la Activity
import android.view.View; // 👆 Para manejar eventos de clic
import android.widget.AdapterView; // 🎚 Interfaz para manejar clics en ítems de lista
import android.widget.ArrayAdapter; // 📋 Adaptador simple para mostrar objetos en una lista
import android.widget.ListView; // 📜 Lista visual donde veremos los tanques
import android.widget.Toast; // 🍞 Mensajes cortos que aparecen como “avisitos”

import androidx.activity.EdgeToEdge; // 📱 Permite usar toda la pantalla
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Base de una Activity moderna
import androidx.core.graphics.Insets; // 📐 Bordes del sistema (status bar, nav bar)
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 Manejo de autenticación de usuarios
import com.google.firebase.database.DataSnapshot; // 📦 Datos que vienen desde Firebase
import com.google.firebase.database.DatabaseError; // 🚫 Error de Firebase
import com.google.firebase.database.DatabaseReference; // 📍 Puntero a una ruta de la BD
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos completa
import com.google.firebase.database.ValueEventListener; // 👂 Escuchador para leer datos

import java.util.ArrayList; // 📚 Lista dinámica para guardar muchos tanques

// 🧾 Pantalla LISTA: aquí mostramos todos los tanques del usuario en una lista 📜💧
public class Lista extends AppCompatActivity {

    private ListView listView; // 📜 La vista donde se muestran los tanques
    private ArrayList<TanqueAgua> listaTanques; // 🏺 Lista en memoria con los tanques
    private ArrayAdapter<TanqueAgua> adapter; // 🔗 Adaptador para conectar datos con la ListView

    private FirebaseAuth mAuth; // 🔐 Ver quién es el usuario actual
    private DatabaseReference usuariosRef; // 📍 Referencia a /usuarios/{uid}/tanques

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Se llama cuando se abre esta pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Activamos diseño de borde a borde
        setContentView(R.layout.activity_lista); // 🎨 Cargamos el diseño de la lista

        // 📐 Ajustamos los márgenes para no chocar con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // 🎯 Conectamos el ListView del XML con la variable de Java
        listView = findViewById(R.id.listaTanques);

        // 🧺 Creamos la lista vacía que tendrá todos los tanques
        listaTanques = new ArrayList<>();

        // 🔗 Adaptador básico que mostrará el texto devuelto por toString() de TanqueAgua
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaTanques);
        listView.setAdapter(adapter); // 🔌 Conectamos el adaptador a la lista visual

        // 🔐 Obtenemos el usuario actual
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid(); // 🆔 ID único del usuario

        // 🗺 Apuntamos a: /usuarios/{uid}/tanques en Firebase
        usuariosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        // 📥 Cargamos los tanques desde Firebase
        cargarTanques();

        // 👆 Cuando el usuario toca un tanque de la lista...
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 📦 Obtenemos el tanque que se tocó
            TanqueAgua tanqueSeleccionado = listaTanques.get(position);

            if (tanqueSeleccionado == null) { // 😱 Por si acaso, chequeamos null
                Toast.makeText(Lista.this, "Error al seleccionar tanque", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🚀 Vamos a la pantalla de INFORMACIÓN del tanque (detalle)
            // ⚠️ IMPORTANTE: Aquí debe ir una ACTIVITY, no la clase modelo.
            Intent intent = new Intent(Lista.this, Informacion.class);

            // ✉️ Enviamos el ID del tanque para que la otra pantalla sepa qué leer de Firebase
            intent.putExtra("tanqueId", tanqueSeleccionado.getIdTanque());

            // 📝 También podemos mandar el nombre, capacidad y color como “extras” para precargar datos
            intent.putExtra("tanqueNombre", tanqueSeleccionado.getNombre());
            intent.putExtra("tanqueCapacidad", tanqueSeleccionado.getCapacidad());
            intent.putExtra("tanqueColor", tanqueSeleccionado.getColor());

            startActivity(intent); // ▶ Abrimos la pantalla Informacion
        });
    }

    // 📥 Función que lee los tanques desde Firebase y llena la lista
    private void cargarTanques() {
        // 👂 Leemos una vez todos los tanques del usuario
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) { // 📦 Respuesta con los datos
                listaTanques.clear(); // 🧹 Limpiamos la lista actual

                if (snapshot.exists()) { // ✅ Si hay tanques guardados…
                    for (DataSnapshot tanqueSnap : snapshot.getChildren()) {
                        // 🧱 Convertimos cada nodo en un objeto TanqueAgua
                        TanqueAgua tanque = tanqueSnap.getValue(TanqueAgua.class);
                        if (tanque != null) {

                            // 🆔 Si el objeto no trae idTanque, usamos la key del nodo
                            if (tanque.getIdTanque() == null || tanque.getIdTanque().isEmpty()) {
                                tanque.setIdTanque(tanqueSnap.getKey());
                            }

                            // ➕ Lo agregamos a la lista en memoria
                            listaTanques.add(tanque);
                        }
                    }
                    // 🔁 Avisamos al adaptador que los datos cambiaron para actualizar la UI
                    adapter.notifyDataSetChanged();
                } else {
                    // 😢 No hay tanques para este usuario
                    Toast.makeText(Lista.this, "No hay tanques registrados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { // 💥 Si algo sale mal
                Toast.makeText(Lista.this,
                        "Error al cargar tanques: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
