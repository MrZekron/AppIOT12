package com.example.appiot12.ui.maps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaDireccionActivity extends AppCompatActivity {

    private MapView map;
    private Marker markerSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.cliente_mapa_direccion);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(16.0);
        map.getController().setCenter(new GeoPoint(-33.4489, -70.6693));

        map.setOnTouchListener((v, event) -> {
            GeoPoint punto = (GeoPoint) map.getProjection().fromPixels(
                    (int) event.getX(), (int) event.getY());
            seleccionarPunto(punto);
            return false;
        });
    }

    private void seleccionarPunto(GeoPoint punto) {
        if (markerSeleccionado != null) map.getOverlays().remove(markerSeleccionado);

        markerSeleccionado = new Marker(map);
        markerSeleccionado.setPosition(punto);
        markerSeleccionado.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerSeleccionado.setTitle("Ubicación seleccionada");

        map.getOverlays().add(markerSeleccionado);
        map.invalidate();

        Intent data = new Intent();
        data.putExtra("latitud", punto.getLatitude());
        data.putExtra("longitud", punto.getLongitude());

        setResult(RESULT_OK, data);
        Toast.makeText(this, "Ubicación seleccionada", Toast.LENGTH_SHORT).show();
        finish();
    }
}
