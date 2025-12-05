package com.example.appiot12; // 📦 paquete del proyecto

import android.os.Bundle; // 🎒 estado de la Activity
import android.widget.ListView; // 📜 lista donde veremos los pagos
import android.widget.Toast; // 🍞 mensajitos

import androidx.activity.EdgeToEdge; // ↔️ diseño sin bordes
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 auth del usuario
import com.google.firebase.database.DataSnapshot; // 📦 datos leídos
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference; // 📍 nodo de la DB
import com.google.firebase.database.FirebaseDatabase; // ☁ base de datos
import com.google.firebase.database.ValueEventListener; // 👂 escucha cambios

import java.util.ArrayList; // 🗂 lista dinámica

public class HistorialCompra extends AppCompatActivity {

    private ListView lvPagos; // 📜 listview donde mostraremos el historial
    private ArrayList<Pago> pagosList = new ArrayList<>(); // 🗂 lista en memoria

    private PagoAdapter pagoAdapter; // 🎨 adaptador que pintará cada pago
    private DatabaseReference pagosRef; // 📌 referencia a usuarios/{uid}/pagos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_compra);

        // 🔧 Ajustar bordes de pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 📍 Referencia al ListView
        lvPagos = findViewById(R.id.lvPagos);

        // 🎨 Creamos el adaptador pasando el contexto y la lista vacía
        pagoAdapter = new PagoAdapter(this, pagosList);
        lvPagos.setAdapter(pagoAdapter);

        // 🔐 UID del usuario actual
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 📌 Ruta clave: usuarios/{uid}/pagos
        pagosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");

        // 🚀 Cargar historial desde Firebase
        cargarPagosUsuario();
    }

    // 📥 Leer pagos desde Firebase
    private void cargarPagosUsuario() {

        pagosRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pagosList.clear(); // 🧹 limpiamos antes de llenar

                if (!snapshot.exists()) {
                    Toast.makeText(HistorialCompra.this,
                            "No hay historial disponible",
                            Toast.LENGTH_LONG).show();
                    pagoAdapter.notifyDataSetChanged();
                    return;
                }

                // 🎯 Recorremos cada "idPago"
                for (DataSnapshot pagoSnap : snapshot.getChildren()) {
                    Pago pago = pagoSnap.getValue(Pago.class);

                    if (pago != null) {
                        pagosList.add(pago);
                    }
                }

                // 🔄 Recargamos la lista visual
                pagoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialCompra.this,
                        "Error al cargar historial: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
