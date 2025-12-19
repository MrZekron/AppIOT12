package com.example.appiot12;
// 🌍 Servicio simple para validar direcciones reales usando Android Geocoder

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

/**
 * 🗺️ GeocodingService
 *
 * Explicado para niño 👶:
 * 👉 Le damos una dirección escrita
 * 👉 Android va al mapa
 * 👉 Si existe → nos devuelve coordenadas
 * 👉 Si no existe → dice "no encontré nada" ❌
 *
 * ✔ Gratis
 * ✔ Sin pagar
 * ✔ Sin Google Maps API
 */
public class GeocodingService {

    /**
     * 📍 Convierte una dirección en coordenadas
     *
     * @param context   Activity o App
     * @param direccion Texto escrito por el usuario
     * @return double[]{lat, lng} o null si no existe
     */
    public static double[] obtenerCoordenadas(Context context, String direccion) {

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> resultados =
                    geocoder.getFromLocationName(direccion, 1);

            // ❌ Dirección no encontrada
            if (resultados == null || resultados.isEmpty()) {
                return null;
            }

            Address address = resultados.get(0);

            return new double[]{
                    address.getLatitude(),
                    address.getLongitude()
            };

        } catch (Exception e) {
            return null; // 🚫 Error = dirección inválida
        }
    }
}
