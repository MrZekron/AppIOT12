package com.example.appiot12.ui.tanque;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.example.appiot12.model.Dispositivo;
import com.example.appiot12.model.TanqueAgua;
import com.example.appiot12.service.HistorialService;
import com.example.appiot12.ui.menu.Menu;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Agregar extends BaseActivity {

    private DatabaseReference databaseReference;

    private EditText txtNombre, txtCapasidad, txtDireccion;
    private Spinner spnColor;

    private boolean direccionValidada = false;
    private String direccionFormateada = "";
    private String placeIdDireccion = "";
    private double latitudDireccion = 0.0;
    private double longitudDireccion = 0.0;

    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        try {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Place place = Autocomplete.getPlaceFromIntent(result.getData());
                                if (place.getAddress() != null && place.getLatLng() != null) {
                                    direccionFormateada = place.getAddress();
                                    placeIdDireccion = place.getId() != null ? place.getId() : "";
                                    latitudDireccion = place.getLatLng().latitude;
                                    longitudDireccion = place.getLatLng().longitude;
                                    direccionValidada = true;
                                    txtDireccion.setText(direccionFormateada);
                                    txtDireccion.setSelection(txtDireccion.getText().length());
                                    toast("Dirección validada correctamente");
                                } else {
                                    limpiarDireccionValidada();
                                    toast("No se pudo validar la dirección");
                                }
                            }
                        } catch (Exception e) {
                            limpiarDireccionValidada();
                            toast("Error al validar dirección: " + e.getMessage());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.cliente_agregar_tanque);

            txtNombre = findViewById(R.id.txtNombre);
            txtCapasidad = findViewById(R.id.txtCapasidad);
            txtDireccion = findViewById(R.id.txtDireccion);
            spnColor = findViewById(R.id.spnColor);

            iniciarFirebase();
            iniciarPlaces();
            cargarColores();
            configurarCampoDireccion();
        } catch (Exception e) {
            toast("Error al abrir Agregar: " + e.getMessage());
            finish();
        }
    }

    private void iniciarFirebase() {
        FirebaseApp.initializeApp(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void iniciarPlaces() {
        String apiKey = getString(R.string.google_maps_key);
        if (TextUtils.isEmpty(apiKey) || "TU_API_KEY_AQUI".equals(apiKey)) {
            toast("Configura tu API Key de Google Places");
            return;
        }
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
    }

    private void cargarColores() {
        String[] colores = {
            "Seleccione un color", "Azul", "Celeste", "Verde", "Rojo",
            "Amarillo", "Naranjo", "Blanco", "Negro", "Gris", "Café", "Morado", "Rosado"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnColor.setAdapter(adapter);
    }

    private void configurarCampoDireccion() {
        txtDireccion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (direccionValidada && !s.toString().equals(direccionFormateada)) {
                    limpiarDireccionValidada();
                }
            }
        });
    }

    public void validarDireccion(View view) {
        String consulta = txtDireccion.getText().toString().trim();
        if (consulta.isEmpty()) {
            txtDireccion.setError("Ingrese una dirección");
            txtDireccion.requestFocus();
            return;
        }

        String apiKey = getString(R.string.google_maps_key);
        if (TextUtils.isEmpty(apiKey) || "TU_API_KEY_AQUI".equals(apiKey)) {
            toast("Debes configurar la API Key de Google Places");
            return;
        }

        try {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountries(Arrays.asList("CL"))
                    .setInitialQuery(consulta)
                    .build(this);
            autocompleteLauncher.launch(intent);
        } catch (Exception e) {
            toast("Error al abrir Google Places: " + e.getMessage());
        }
    }

    private void limpiarDireccionValidada() {
        direccionValidada = false;
        direccionFormateada = "";
        placeIdDireccion = "";
        latitudDireccion = 0.0;
        longitudDireccion = 0.0;
    }

    public void enviarDatosUsuario(View view) {
        String nombre = txtNombre.getText().toString().trim();
        String capacidad = txtCapasidad.getText().toString().trim();
        String color = spnColor.getSelectedItem().toString();
        String direccionVisible = txtDireccion.getText().toString().trim();

        if (!validarCampos(nombre, capacidad, color, direccionVisible)) return;

        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual == null) {
            toast("Error: usuario no autenticado");
            return;
        }

        String uid = usuarioActual.getUid();
        String idTanque = UUID.randomUUID().toString();
        String idDispositivo = UUID.randomUUID().toString();

        Dispositivo dispositivo = new Dispositivo(idDispositivo, 7.0, 500.0, 1.0, 150.0);

        DatabaseReference dispositivoRef = databaseReference.child("usuarios").child(uid).child("dispositivos").child(idDispositivo);
        DatabaseReference tanqueRef = databaseReference.child("usuarios").child(uid).child("tanques").child(idTanque);

        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);
        tanque.setIdDispositivo(idDispositivo);

        dispositivoRef.setValue(dispositivo)
                .addOnSuccessListener(aVoid ->
                        tanqueRef.setValue(tanque)
                                .addOnSuccessListener(aVoid1 -> {
                                    Map<String, Object> datosDireccion = new HashMap<>();
                                    datosDireccion.put("direccion", direccionFormateada);
                                    datosDireccion.put("latitud", latitudDireccion);
                                    datosDireccion.put("longitud", longitudDireccion);
                                    datosDireccion.put("placeIdDireccion", placeIdDireccion);

                                    tanqueRef.updateChildren(datosDireccion)
                                            .addOnSuccessListener(aVoid2 -> {
                                                HistorialService.registrarEvento("CREAR", "Se creó el tanque: " + nombre);
                                                toast("Tanque creado correctamente");
                                                startActivity(new Intent(Agregar.this, Lista.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    toast("Tanque creado, pero falló la dirección: " + e.getMessage()));
                                })
                                .addOnFailureListener(e -> toast("Error al guardar tanque: " + e.getMessage()))
                )
                .addOnFailureListener(e -> toast("Error al guardar dispositivo: " + e.getMessage()));
    }

    private boolean validarCampos(String nombre, String capacidad, String color, String direccionVisible) {
        if (nombre.isEmpty()) {
            txtNombre.setError("Ingrese el nombre del tanque");
            txtNombre.requestFocus();
            return false;
        }
        if (capacidad.isEmpty()) {
            txtCapasidad.setError("Ingrese la capacidad del tanque");
            txtCapasidad.requestFocus();
            return false;
        }
        if (!capacidad.matches("\\d+")) {
            txtCapasidad.setError("Solo se permiten números");
            txtCapasidad.requestFocus();
            return false;
        }
        if (Integer.parseInt(capacidad) <= 0) {
            txtCapasidad.setError("La capacidad debe ser mayor a 0");
            txtCapasidad.requestFocus();
            return false;
        }
        if ("Seleccione un color".equals(color)) {
            toast("Seleccione un color para el tanque");
            return false;
        }
        if (direccionVisible.isEmpty()) {
            txtDireccion.setError("Ingrese una dirección");
            txtDireccion.requestFocus();
            return false;
        }
        if (!direccionValidada || !direccionVisible.equals(direccionFormateada)) {
            txtDireccion.setError("Debe validar la dirección con el botón");
            txtDireccion.requestFocus();
            toast("Primero valida la dirección");
            return false;
        }
        return true;
    }

    public void verLista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    public void cancelar(View view) {
        startActivity(new Intent(this, Menu.class));
        finish();
    }

    private void toast(@NonNull String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
