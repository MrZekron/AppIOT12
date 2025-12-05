package com.example.appiot12; // 📦 Aquí guardamos este archivo dentro del paquete de la app

import android.content.Intent; // 🚪 Para movernos entre pantallas (Activities)
import android.os.Bundle; // 🎒 Para recibir datos del sistema cuando la pantalla inicia
import android.view.View; // 👆 Para detectar clics
import android.widget.EditText; // 📝 Para leer texto que escribe el usuario
import android.widget.Toast; // 🍞 Mensajes cortos que aparecen en pantalla "toastiados"

import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clase base para pantallas modernas

import com.google.firebase.FirebaseApp; // 🚀 Para usar Firebase
import com.google.firebase.auth.FirebaseAuth; // 🔐 Para saber qué usuario está conectado
import com.google.firebase.database.DatabaseReference; // 🗂️ Para apuntar a un nodo de la DB
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos completa

import java.util.UUID; // 🆔 Para crear códigos únicos mágicos ✨

public class Agregar extends AppCompatActivity { // 🌟 Pantalla para agregar tanques

    // 🔌 Variables para conectar a Firebase Realtime Database
    private FirebaseDatabase fdbd; // 🛢️ Base de datos
    private DatabaseReference dbrf; // 🗃️ Un "puntero" a un lugar dentro de la base

    // 📝 Cajas de texto donde el usuario escribe información
    private EditText txtNombre, txtCapasidad, txtColor, txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Esto se ejecuta al abrir la pantalla
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar); // 🎨 Ponemos el diseño

        // 🔍 Buscamos los elementos del diseño y los conectamos con las variables
        txtNombre = findViewById(R.id.txtNombre); // 📝 Nombre del tanque
        txtCapasidad = findViewById(R.id.txtCapasidad); // 💧 Capacidad
        txtColor = findViewById(R.id.txtColor); // 🎨 Color
        txtDireccion = findViewById(R.id.txtDireccion); // 📍 Dirección opcional

        iniciarFirebase(); // 🚀 Arrancamos Firebase
    }

    private void iniciarFirebase() { // 🔧 Activamos Firebase
        FirebaseApp.initializeApp(this); // 🎛️ Configura Firebase en la app
        fdbd = FirebaseDatabase.getInstance(); // 🛢️ Obtenemos la base de datos completa
        dbrf = fdbd.getReference(); // 🗺️ Apuntamos a la raíz de la base
    }

    public void enviarDatosUsuario(View view) { // 📤 Se ejecuta cuando el niño oprime el botón "Agregar"

        // 📌 Leemos lo que escribió el usuario
        String nombre = txtNombre.getText().toString().trim(); // ✍️
        String color = txtColor.getText().toString().trim(); // 🎨
        String capacidad = txtCapasidad.getText().toString().trim(); // 💧
        String direccion = txtDireccion.getText().toString().trim(); // 📍

        // 🚨 Revisamos que no falten datos
        if (nombre.isEmpty() || color.isEmpty() || capacidad.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return; // 🛑 Detenemos todo
        }

        // 🔐 Obtenemos el UID del usuario actual (su "cédula digital")
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid == null) { // 😱 Si no hay usuario logueado
            Toast.makeText(this, "Error: usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return; // 🛑
        }

        // 🆔 Creamos un ID único para el tanque (así no se repite con otros tanques)
        String idTanque = UUID.randomUUID().toString();

        // 🧪 Creamos un dispositivo con valores iniciales
        Dispositivo d1 = new Dispositivo(); // 🔧 Nuevo dispositivo
        d1.setId(UUID.randomUUID().toString()); // 🆔 ID único del dispositivo
        d1.setPh(7.0); // ⚗️ pH inicial "perfectito"
        d1.setConductividad(500.0); // ⚡ Valor normal
        d1.setTurbidez(1.0); // 🌫️ Agua clarita
        d1.setUltrasonico(150.0); // 📏 Nivel de agua inicial

        // 🧱 Creamos un tanque y le metemos todos los datos
        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);
        tanque.setDispositivo(d1); // 🔌 Conectamos el dispositivo

        // 🛣️ Ruta donde se guardará en Firebase:
        // usuarios/{uid}/tanques/{idTanque}
        DatabaseReference ref = dbrf.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque);

        // 🎉 Guardamos el tanque en Firebase
        ref.setValue(tanque)
                .addOnSuccessListener(aVoid -> { // ✔️ Si todo salió bien:
                    Toast.makeText(Agregar.this, "Tanque agregado correctamente.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Agregar.this, Lista.class)); // 📋 Vamos a la pantalla de lista
                    finish(); // 🚪 Cerramos esta pantalla
                })
                .addOnFailureListener(e -> // ❌ Si hubo un error:
                        Toast.makeText(Agregar.this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    public void verLista(View v) { // 📋 Botón "Ver lista"
        startActivity(new Intent(this, Lista.class)); // 📲 Abrimos la pantalla Lista
    }

    public void cancelar(View view) { // ❌ Botón cancelar
        startActivity(new Intent(this, Menu.class)); // 🏠 Volvemos al menú
        finish(); // 🚪 Cerramos esta pantalla
    }
}
