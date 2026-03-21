const functions = require("firebase-functions/v2/https");
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