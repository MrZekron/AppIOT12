package com.example.appiot12.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.appiot12.model.LecturaParametro;
import com.example.appiot12.model.TanqueAgua;
import com.example.appiot12.service.AlertaService;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlertaWorker extends Worker {

    private static final String TAG = "AlertaWorker";

    public AlertaWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return Result.success();

            String uid = user.getUid();
            DatabaseReference tanquesRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).child("tanques");

            DataSnapshot tanquesSnap = Tasks.await(tanquesRef.get());
            if (!tanquesSnap.exists()) return Result.success();

            for (DataSnapshot tanqueSnap : tanquesSnap.getChildren()) {
                TanqueAgua tanque = tanqueSnap.getValue(TanqueAgua.class);
                if (tanque == null) continue;

                String idDispositivo = tanque.getIdDispositivo();
                if (idDispositivo == null || idDispositivo.trim().isEmpty()) continue;

                DataSnapshot lecturaSnap = Tasks.await(
                        FirebaseDatabase.getInstance()
                                .getReference("lecturas_actuales")
                                .child(idDispositivo)
                                .get()
                );
                if (!lecturaSnap.exists()) continue;

                LecturaParametro lectura = lecturaSnap.getValue(LecturaParametro.class);
                if (lectura == null) continue;

                AlertaService.evaluarLectura(
                        getApplicationContext(),
                        uid,
                        idDispositivo,
                        lectura.getPh(),
                        lectura.getConductividad(),
                        lectura.getTurbidez(),
                        lectura.getNivelPorcentaje()
                );
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            return Result.retry();
        }
    }
}
