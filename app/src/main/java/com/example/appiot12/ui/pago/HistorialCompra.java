package com.example.appiot12.ui.pago;
// 📦 Pantalla de historial de compras del proyecto Agua Segura.
// Aquí el usuario puede ver todos sus pagos realizados 💸📘

// ===== IMPORTS ANDROID =====
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
// 🏛 Activity base estable

// ===== IMPORTS FIREBASE =====
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ☁️ Firebase Realtime Database

// ===== IMPORTS PROYECTO =====
import com.example.appiot12.R;
import com.example.appiot12.adapter.PagoAdapter;
import com.example.appiot12.model.Pago;

// ===== IMPORTS JAVA =====
import java.util.ArrayList;
import java.util.List;
// 🗂️ Listas dinámicas

/**
 * 💳 HistorialCompra
 *
 * ¿Qué hace esta pantalla?
 * 👉 Muestra todas las compras/pagos del usuario
 * 👉 Lee los datos desde Firebase
 * 👉 Los muestra en una lista ordenada
 *
 * Explicado para un niño:
 * 👉 Es como mirar el cuaderno donde anotas
 *    todo lo que has comprado 📒🙂
 */
public class HistorialCompra extends AppCompatActivity {

    // 📋 Lista visual donde se muestran los pagos
    private ListView lvPagos;

    // 🗂️ Lista en memoria con los pagos del usuario
    private final List<Pago> pagos = new ArrayList<>();

    // 🎨 Adaptador que convierte Pago → fila visual
    private PagoAdapter pagoAdapter;

    // ☁️ Referencia a los pagos en Firebase
    private DatabaseReference refPagos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_compra); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos UI con el XML
        inicializarVistas();

        // 👤 Obtenemos UID del usuario
        String uid = obtenerUidUsuario();

        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ☁️ Apuntamos a /usuarios/{uid}/pagos
        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");

        // 🎨 Creamos y asignamos el adaptador
        pagoAdapter = new PagoAdapter(this, pagos);
        lvPagos.setAdapter(pagoAdapter);

        // 📥 Cargamos los pagos desde Firebase
        cargarPagos();
    }

    /**
     * 🔗 Vincula el ListView con el XML
     */
    private void inicializarVistas() {
        lvPagos = findViewById(R.id.lvPagos);
    }

    /**
     * 👤 Obtiene el UID del usuario autenticado
     */
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 📥 CARGAR PAGOS DESDE FIREBASE
    // =====================================================
    private void cargarPagos() {

        refPagos.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                pagos.clear(); // ♻️ Limpiamos la lista antes de recargar

                if (!snapshot.exists()) {
                    Toast.makeText(
                            HistorialCompra.this,
                            "No tienes pagos registrados 📭",
                            Toast.LENGTH_LONG
                    ).show();
                    pagoAdapter.notifyDataSetChanged();
                    return;
                }

                // 🔄 Recorremos cada pago guardado
                for (DataSnapshot pagoSnap : snapshot.getChildren()) {

                    Pago pago = pagoSnap.getValue(Pago.class);

                    if (pago != null) {
                        pagos.add(pago); // 💾 Agregamos a la lista
                    }
                }

                // 📭 Si la lista quedó vacía
                if (pagos.isEmpty()) {
                    Toast.makeText(
                            HistorialCompra.this,
                            "No tienes pagos registrados 📭",
                            Toast.LENGTH_LONG
                    ).show();
                }

                // 🔄 Actualizamos la lista visual
                pagoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        HistorialCompra.this,
                        "Error al leer pagos ⚠️",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
