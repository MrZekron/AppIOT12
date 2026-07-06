package com.example.appiot12.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.example.appiot12.ui.BaseActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.pago.CentroPagos;
import com.example.appiot12.ui.pago.ComprarDispositivo;
import com.example.appiot12.ui.pago.HistorialCompra;
import com.example.appiot12.ui.perfil.Configuracion;
import com.example.appiot12.ui.tanque.Agregar;
import com.example.appiot12.ui.tanque.AsociarDispositivoATanque;
import com.example.appiot12.ui.tanque.Lista;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Menu extends BaseActivity {

    private FirebaseAuth auth;
    private TextView tvCorreoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cliente_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        mostrarCorreoUsuario();
    }

    private void mostrarCorreoUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvCorreoUsuario.setText("Sin sesión activa");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String correo = user.getEmail();
        tvCorreoUsuario.setText((correo != null && !correo.isEmpty()) ? correo : "Sesión activa");
    }

    private void irA(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    public void abrirConfiguracion(View v) { irA(Configuracion.class); }
    public void agregar(View v)            { irA(Agregar.class); }
    public void lista(View v)              { irA(Lista.class); }
    public void pagos(View v)              { irA(HistorialCompra.class); }
    public void comprarDispositivo(View v) { irA(ComprarDispositivo.class); }
    public void asociarDispositivo(View v) { irA(AsociarDispositivoATanque.class); }
    public void centroPagos(View v)        { irA(CentroPagos.class); }

    public void salir(View v) {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
