package com.example.appiot12;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HistorialAcciones extends AppCompatActivity {

    private ListView lvHistorial;
    private ArrayList<AccionLog> acciones = new ArrayList<>();
    private AccionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_acciones);

        lvHistorial = findViewById(R.id.lvHistorial);
        adapter = new AccionAdapter(this, acciones);
        lvHistorial.setAdapter(adapter);

        cargarHistorial();
    }

    private void cargarHistorial() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        long treintaDias = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        ref.orderByChild("timestamp")
                .startAt(treintaDias)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        acciones.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            AccionLog log = s.getValue(AccionLog.class);
                            if (log != null) acciones.add(log);
                        }

                        if (acciones.isEmpty()) {
                            Toast.makeText(HistorialAcciones.this,
                                    "No hay acciones registradas en los últimos 30 días",
                                    Toast.LENGTH_LONG).show();
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
}
