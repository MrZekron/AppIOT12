package com.example.appiot12.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

public class GeocodingService {

    public static double[] obtenerCoordenadas(Context context, String direccion) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> resultados = geocoder.getFromLocationName(direccion, 1);

            if (resultados == null || resultados.isEmpty()) return null;

            Address address = resultados.get(0);
            return new double[]{ address.getLatitude(), address.getLongitude() };
        } catch (Exception e) {
            return null;
        }
    }
}
