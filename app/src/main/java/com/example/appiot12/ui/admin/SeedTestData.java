package com.example.appiot12.ui.admin;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/** Datos de prueba para ejemplo@gmail.com — borrar antes de producción */
public class SeedTestData {

    private static final String UID = "imcSt8bHlzfSOHkA2jtukOVVdm22";

    public static void sembrar(Context ctx) {
        long now        = System.currentTimeMillis();
        long hace45dias = now - (45L * 24 * 60 * 60 * 1000);
        long hace75dias = now - (75L * 24 * 60 * 60 * 1000);

        Map<String, Object> data = new HashMap<>();

        // ── DISPOSITIVOS ─────────────────────────────────────────────────────
        Map<String, Object> disp1 = new HashMap<>();
        disp1.put("id", "AGS-001");
        disp1.put("activo", true);
        disp1.put("estado", "activo");
        disp1.put("fechaInstalacion", now);

        Map<String, Object> disp2 = new HashMap<>();
        disp2.put("id", "AGS-002");
        disp2.put("activo", true);
        disp2.put("estado", "activo");
        disp2.put("fechaInstalacion", hace45dias);
        disp2.put("idTanque", "tanque-prueba-01");

        data.put("usuarios/" + UID + "/dispositivos/AGS-001", disp1);
        data.put("usuarios/" + UID + "/dispositivos/AGS-002", disp2);

        // ── TANQUES ───────────────────────────────────────────────────────────
        Map<String, Object> tanque1 = new HashMap<>();
        tanque1.put("idTanque", "tanque-prueba-01");
        tanque1.put("nombre", "Tanque Principal");
        tanque1.put("capacidadLitros", 1000);
        tanque1.put("color", "Azul");
        tanque1.put("direccion", "Av. Las Pruebas 123, Santiago");
        tanque1.put("idDispositivo", "AGS-002");
        tanque1.put("mantencionTanque", false);
        tanque1.put("mantencionDispositivo", false);

        Map<String, Object> tanque2 = new HashMap<>();
        tanque2.put("idTanque", "tanque-prueba-02");
        tanque2.put("nombre", "Tanque Secundario");
        tanque2.put("capacidadLitros", 500);
        tanque2.put("color", "Blanco");
        tanque2.put("direccion", "Calle Ficticia 456, Valparaíso");
        tanque2.put("mantencionTanque", true);
        tanque2.put("mantencionDispositivo", false);

        data.put("usuarios/" + UID + "/tanques/tanque-prueba-01", tanque1);
        data.put("usuarios/" + UID + "/tanques/tanque-prueba-02", tanque2);

        // ── PAGOS ─────────────────────────────────────────────────────────────
        // Pago 1: 6 cuotas, 2 pagadas → 4 pendientes, deuda $86,667, atrasado 45 días
        Map<String, Object> pago1 = new HashMap<>();
        pago1.put("idPago", "pago-test-001");
        pago1.put("idCliente", UID);
        pago1.put("idDispositivo", "AGS-001");
        pago1.put("nombreProducto", "Dispositivo AguaSegura");
        pago1.put("precioTotal", 130000);
        pago1.put("cuotasTotales", 6);
        pago1.put("cuotasPagadas", 2);
        pago1.put("saldoPendiente", 86667);
        pago1.put("estadoPago", "pendiente");
        pago1.put("pagado", false);
        pago1.put("fechaCreacion", hace45dias);
        pago1.put("ultimaActualizacion", hace45dias);

        // Pago 2: 3 cuotas, 3 pagadas → al día, saldo $0
        Map<String, Object> pago2 = new HashMap<>();
        pago2.put("idPago", "pago-test-002");
        pago2.put("idCliente", UID);
        pago2.put("idDispositivo", "AGS-002");
        pago2.put("nombreProducto", "Dispositivo AguaSegura");
        pago2.put("precioTotal", 130000);
        pago2.put("cuotasTotales", 3);
        pago2.put("cuotasPagadas", 3);
        pago2.put("saldoPendiente", 0);
        pago2.put("estadoPago", "pagado");
        pago2.put("pagado", true);
        pago2.put("fechaCreacion", hace75dias);
        pago2.put("ultimaActualizacion", hace45dias);

        data.put("usuarios/" + UID + "/pagos/pago-test-001", pago1);
        data.put("usuarios/" + UID + "/pagos/pago-test-002", pago2);

        // ── COMPRAS ───────────────────────────────────────────────────────────
        Map<String, Object> compra1 = new HashMap<>();
        compra1.put("idCompra", "compra-test-001");
        compra1.put("uidUsuario", UID);
        compra1.put("producto", "Dispositivo AguaSegura");
        compra1.put("monto", 130000);
        compra1.put("idDispositivo", "AGS-001");
        compra1.put("estado", "pendiente_pago");
        compra1.put("fecha", hace45dias);

        Map<String, Object> compra2 = new HashMap<>();
        compra2.put("idCompra", "compra-test-002");
        compra2.put("uidUsuario", UID);
        compra2.put("producto", "Dispositivo AguaSegura");
        compra2.put("monto", 130000);
        compra2.put("idDispositivo", "AGS-002");
        compra2.put("estado", "pagado");
        compra2.put("fecha", hace75dias);

        data.put("compras/compra-test-001", compra1);
        data.put("compras/compra-test-002", compra2);

        // ── LECTURAS EN TIEMPO REAL (sensor AGS-002 en tanque principal) ──────
        data.put("lecturas_actuales/AGS-002/ph", 7.2);
        data.put("lecturas_actuales/AGS-002/conductividad", 450.0);
        data.put("lecturas_actuales/AGS-002/turbidez", 2.1);
        data.put("lecturas_actuales/AGS-002/ultrasonico", 78.5);
        data.put("lecturas_actuales/AGS-002/nivelPorcentaje", 78.5);
        data.put("lecturas_actuales/AGS-002/timestamp", now);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(ctx, "Datos de prueba generados correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
