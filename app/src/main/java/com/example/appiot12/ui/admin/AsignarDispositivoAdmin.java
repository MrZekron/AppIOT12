package com.example.appiot12.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.model.Usuario;
import com.example.appiot12.ui.BaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsignarDispositivoAdmin extends BaseActivity {

    private EditText etDeviceId;
    private EditText etPrecio;
    private AutoCompleteTextView actvUsuario;
    private TextView tvUsuarioSeleccionado;
    private Button btnGenerar;
    private Button btnAsignar;

    private final Map<String, Usuario> mapaUsuarios = new LinkedHashMap<>();
    private Usuario usuarioSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_asignar_dispositivo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        etDeviceId          = findViewById(R.id.etDeviceId);
        etPrecio            = findViewById(R.id.etPrecio);
        actvUsuario         = findViewById(R.id.actvUsuario);
        tvUsuarioSeleccionado = findViewById(R.id.tvUsuarioSeleccionado);
        btnGenerar          = findViewById(R.id.btnGenerar);
        btnAsignar          = findViewById(R.id.btnAsignar);

        cargarPrecioGlobal();
        cargarUsuarios();

        btnGenerar.setOnClickListener(v -> generarId());
        btnAsignar.setOnClickListener(v -> asignar());

        actvUsuario.setOnItemClickListener((parent, view, pos, id) -> {
            String clave = (String) parent.getItemAtPosition(pos);
            usuarioSeleccionado = mapaUsuarios.get(clave);
            if (usuarioSeleccionado != null) {
                tvUsuarioSeleccionado.setText("Seleccionado: " + clave);
            }
        });

        actvUsuario.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                usuarioSeleccionado = null;
                tvUsuarioSeleccionado.setText("");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void generarId() {
        etDeviceId.setText("AGS-" + System.currentTimeMillis());
    }

    private void cargarPrecioGlobal() {
        FirebaseDatabase.getInstance()
                .getReference("config/precioDispositivo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Integer precio = snapshot.getValue(Integer.class);
                        if (precio != null && precio > 0) {
                            etPrecio.setText(String.valueOf(precio));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void cargarUsuarios() {
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        mapaUsuarios.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Usuario u = snap.getValue(Usuario.class);
                            if (u == null) continue;
                            u.setId(snap.getKey());
                            if (!"usuario".equalsIgnoreCase(u.getRol())) continue;

                            String display = (u.getNombre() != null && !u.getNombre().isEmpty())
                                    ? u.getNombre() + " (" + u.getEmail() + ")"
                                    : u.getEmail();
                            mapaUsuarios.put(display, u);
                        }

                        List<String> claves = new ArrayList<>(mapaUsuarios.keySet());
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AsignarDispositivoAdmin.this,
                                android.R.layout.simple_dropdown_item_1line,
                                claves);
                        actvUsuario.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AsignarDispositivoAdmin.this,
                                "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void asignar() {
        String deviceId  = etDeviceId.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (deviceId.isEmpty()) {
            Toast.makeText(this, "Ingresa o genera un ID de dispositivo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (usuarioSeleccionado == null) {
            Toast.makeText(this, "Selecciona un usuario de la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        int precio;
        try {
            precio = Integer.parseInt(precioStr);
            if (precio <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa un precio válido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAsignar.setEnabled(false);

        String uid = usuarioSeleccionado.getId();
        long now = System.currentTimeMillis();

        String compraId = FirebaseDatabase.getInstance().getReference("compras").push().getKey();
        String pagoId   = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("pagos").push().getKey();

        if (compraId == null || pagoId == null) {
            Toast.makeText(this, "Error al generar IDs, intenta de nuevo", Toast.LENGTH_SHORT).show();
            btnAsignar.setEnabled(true);
            return;
        }

        Map<String, Object> dispositivo = new HashMap<>();
        dispositivo.put("id", deviceId);
        dispositivo.put("activo", true);
        dispositivo.put("estado", "activo");
        dispositivo.put("fechaInstalacion", now);

        Map<String, Object> compra = new HashMap<>();
        compra.put("idCompra", compraId);
        compra.put("uidUsuario", uid);
        compra.put("producto", "Dispositivo AguaSegura");
        compra.put("monto", precio);
        compra.put("idDispositivo", deviceId);
        compra.put("estado", "pendiente_pago");
        compra.put("fecha", now);

        // cuotasTotales=1 por defecto; el usuario elige el plan de pago por su cuenta
        Map<String, Object> pago = new HashMap<>();
        pago.put("idPago", pagoId);
        pago.put("idCliente", uid);
        pago.put("idDispositivo", deviceId);
        pago.put("nombreProducto", "Dispositivo AguaSegura");
        pago.put("precioTotal", precio);
        pago.put("cuotasTotales", 1);
        pago.put("cuotasPagadas", 0);
        pago.put("saldoPendiente", precio);
        pago.put("estadoPago", "pendiente");
        pago.put("pagado", false);
        pago.put("fechaCreacion", now);
        pago.put("ultimaActualizacion", now);

        Map<String, Object> updates = new HashMap<>();
        updates.put("config/precioDispositivo", precio);
        updates.put("usuarios/" + uid + "/dispositivos/" + deviceId, dispositivo);
        updates.put("compras/" + compraId, compra);
        updates.put("usuarios/" + uid + "/pagos/" + pagoId, pago);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Dispositivo asignado correctamente", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnAsignar.setEnabled(true);
                    Toast.makeText(this,
                            "Error al asignar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
