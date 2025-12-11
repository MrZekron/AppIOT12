package com.example.appiot12;
// 📦 Pantalla para visualizar el historial de compras/pagos del usuario.
// El "Libro contable digital" de AguaSegura 💸📘

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistorialCompra extends AppCompatActivity {

    private ListView lvPagos;                    // 📋 Lista visual donde se mostrarán los pagos
    private ArrayList<Pago> pagosList = new ArrayList<>(); // 🗂 Lista dinámica con los pagos
    private PagoAdapter pagoAdapter;             // 🎨 Adaptador para convertir Pago → vista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                 // 📱 Pantalla moderna
        setContentView(R.layout.activity_historial_compra);

        // Ajuste automático del contenido respecto a barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enlazamos el listview y asignamos adaptador
        lvPagos = findViewById(R.id.lvPagos);
        pagoAdapter = new PagoAdapter(this, pagosList);
        lvPagos.setAdapter(pagoAdapter);

        // Obtenemos UID del usuario
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🚀 Cargar pagos correctamente desde Firebase
        cargarPagosCorrectamente(uid);
    }

    // ============================================================================
    // 📥 FUNCIÓN CORREGIDA: CARGA PAGOS DESDE /usuarios/{uid}/pagos
    // ============================================================================
    private void cargarPagosCorrectamente(String uid) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos")   // ← RUTA CORRECTA ✔️
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        pagosList.clear(); // 🔄 Vaciar lista antes de cargar

                        if (!snapshot.exists()) {
                            Toast.makeText(HistorialCompra.this,
                                    "No tienes pagos registrados.",
                                    Toast.LENGTH_LONG).show();
                            pagoAdapter.notifyDataSetChanged();
                            return;
                        }

                        // 🔄 Recorrer cada pago almacenado
                        for (DataSnapshot pagoSnap : snapshot.getChildren()) {

                            Pago pago = pagoSnap.getValue(Pago.class);

                            if (pago != null) {
                                pagosList.add(pago); // 💾 Agregar a la lista visible
                            }
                        }

                        // Si después de la carga no hay nada...
                        if (pagosList.isEmpty()) {
                            Toast.makeText(HistorialCompra.this,
                                    "No tienes pagos registrados.",
                                    Toast.LENGTH_LONG).show();
                        }

                        pagoAdapter.notifyDataSetChanged(); // 🔃 Refrescar lista
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(HistorialCompra.this,
                                "Error al leer pagos: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
