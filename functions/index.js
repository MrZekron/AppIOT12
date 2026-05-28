const functions = require("firebase-functions/v2/https");
const { onValueWritten } = require("firebase-functions/v2/database");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.database();

// =====================================================
// Función auxiliar segura para leer paymentId
// =====================================================
function obtenerPaymentId(req) {
  if (req.body && req.body.data && req.body.data.id) {
    return req.body.data.id;
  }

  if (req.query && req.query["data.id"]) {
    return req.query["data.id"];
  }

  if (req.query && req.query.id) {
    return req.query.id;
  }

  return null;
}

// =====================================================
// WEBHOOK MERCADO PAGO
// =====================================================
exports.mercadoPagoWebhook = functions.onRequest(async (req, res) => {
  try {
    const paymentId = obtenerPaymentId(req);

    if (!paymentId) {
      return res.status(200).send("OK");
    }

    // Consultar pago en Mercado Pago
    const mp = await axios.get(
      "https://api.mercadopago.com/v1/payments/" + paymentId,
      {
        headers: {
          Authorization: "Bearer " + process.env.MERCADOPAGO_ACCESS_TOKEN
        }
      }
    );

    const pagoMP = mp.data;
    const idPago = pagoMP.external_reference;

    if (!idPago) {
      return res.status(200).send("OK");
    }

    // Buscar pago en Firebase
    const usuariosSnap = await db.ref("usuarios").once("value");

    let refPago = null;
    let dataPago = null;
    let uid = null;

    usuariosSnap.forEach(function (u) {
      const p = u.child("pagos/" + idPago);
      if (p.exists()) {
        refPago = db.ref("usuarios/" + u.key + "/pagos/" + idPago);
        dataPago = p.val();
        uid = u.key;
      }
    });

    if (!refPago || !dataPago) {
      return res.status(200).send("OK");
    }

    const estado = pagoMP.status;

    let cuotasPagadas = dataPago.cuotasPagadas || 0;
    let saldo = dataPago.saldoPendiente || dataPago.precioTotal || 0;

    const precioTotal = dataPago.precioTotal || 0;
    const cuotasTotales = dataPago.cuotasTotales || 1;
    const valorCuota = Math.ceil(precioTotal / cuotasTotales);

    // Evitar duplicar el mismo pago MP
    if (estado === "approved" && String(dataPago.mpPaymentId || "") !== String(paymentId)) {
      cuotasPagadas = cuotasPagadas + 1;
      saldo = saldo - valorCuota;
    }

    let estadoFinal = "pendiente";

    if (saldo <= 0) {
      estadoFinal = "pagado";
      saldo = 0;
      cuotasPagadas = cuotasTotales;
    } else if (estado === "approved") {
      estadoFinal = "parcial";
    } else if (estado === "rejected" || estado === "cancelled") {
      estadoFinal = "fallido";
    }

    await refPago.update({
      cuotasPagadas: cuotasPagadas,
      saldoPendiente: saldo,
      estadoPago: estadoFinal,
      ultimaActualizacion: Date.now(),
      mpPaymentId: String(paymentId)
    });

    // Historial automático
    await db.ref("usuarios/" + uid + "/historial").push({
      tipo: "PAGO",
      descripcion: "Pago Mercado Pago: " + estado,
      timestamp: Date.now(),
      monto: pagoMP.transaction_amount || 0
    });

    return res.status(200).send("OK");
  } catch (e) {
    logger.error(e);
    return res.status(500).send("ERROR");
  }
});

// =====================================================
// CREAR LINK DE PAGO DINÁMICO
// =====================================================
exports.crearPreferenciaPago = functions.onRequest(async (req, res) => {
  try {
    const idPago = req.body.idPago;
    const titulo = req.body.titulo;
    const precio = req.body.precio;

    const response = await axios.post(
      "https://api.mercadopago.com/checkout/preferences",
      {
        items: [
          {
            title: titulo,
            quantity: 1,
            unit_price: precio,
            currency_id: "CLP"
          }
        ],
        external_reference: idPago,
        notification_url: "https://TU_URL.cloudfunctions.net/mercadoPagoWebhook"
      },
      {
        headers: {
          Authorization: "Bearer " + process.env.MERCADOPAGO_ACCESS_TOKEN
        }
      }
    );

    return res.json({
      checkoutUrl: response.data.init_point
    });
  } catch (e) {
    logger.error(e);
    return res.status(500).send("ERROR");
  }
});

// =====================================================
// ALERTA CALIDAD DEL AGUA — trigger RTDB
// Escucha: usuarios/{uid}/dispositivos/{idDispositivo}
// Envía FCM si ph/conductividad/turbidez supera umbral
// =====================================================
exports.alertarCalidadAgua = onValueWritten(
  {
    ref: "usuarios/{uid}/dispositivos/{idDispositivo}",
    region: "us-central1"
  },
  async (event) => {
    const after = event.data.after;
    if (!after.exists()) return;

    const uid = event.params.uid;
    const data = after.val();

    const ph = typeof data.ph === "number" ? data.ph : null;
    const conductividad = typeof data.conductividad === "number" ? data.conductividad : null;
    const turbidez = typeof data.turbidez === "number" ? data.turbidez : null;

    const alertas = [];
    let esPeligro = false;

    if (ph !== null) {
      if (ph < 6.0 || ph > 9.0) {
        alertas.push(`pH ${ph.toFixed(2)} — PELIGRO`);
        esPeligro = true;
      } else if (ph < 6.5 || ph > 8.5) {
        alertas.push(`pH ${ph.toFixed(2)} — Alerta`);
      }
    }

    if (conductividad !== null) {
      if (conductividad > 2500) {
        alertas.push(`Conductividad ${conductividad.toFixed(0)} µS/cm — PELIGRO`);
        esPeligro = true;
      } else if (conductividad > 1500) {
        alertas.push(`Conductividad ${conductividad.toFixed(0)} µS/cm — Alerta`);
      }
    }

    if (turbidez !== null) {
      if (turbidez > 10) {
        alertas.push(`Turbidez ${turbidez.toFixed(2)} NTU — PELIGRO`);
        esPeligro = true;
      } else if (turbidez > 5) {
        alertas.push(`Turbidez ${turbidez.toFixed(2)} NTU — Alerta`);
      }
    }

    if (alertas.length === 0) return;

    const tokenSnap = await db.ref(`usuarios/${uid}/fcmToken`).once("value");
    const token = tokenSnap.val();
    if (!token) {
      logger.warn(`Sin fcmToken para uid=${uid}`);
      return;
    }

    const titulo = esPeligro
      ? "Peligro — Calidad del Agua"
      : "Alerta — Calidad del Agua";
    const cuerpo = alertas.join(" | ");

    await admin.messaging().send({
      token,
      notification: { title: titulo, body: cuerpo },
      data: { title: titulo, body: cuerpo },
      android: {
        priority: "high",
        notification: { sound: "default", channel_id: "agua_alertas" }
      }
    });

    logger.info(`FCM enviado a uid=${uid}: ${cuerpo}`);
  }
);