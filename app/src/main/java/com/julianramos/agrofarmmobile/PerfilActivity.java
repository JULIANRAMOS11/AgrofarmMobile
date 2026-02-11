package com.julianramos.agrofarmmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Toolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        TextView tvUsername = findViewById(R.id.tvPerfilUsername);
        TextView tvRole = findViewById(R.id.tvPerfilRole);
        TextView tvInicial = findViewById(R.id.tvInicial);
        TextView tvDisplayUser = findViewById(R.id.tvDisplayUser);
        TextView tvDisplayRole = findViewById(R.id.tvDisplayRole);
        TextView tvDisplayID = findViewById(R.id.tvDisplayID);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        SharedPreferences prefs = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Usuario");
        String role = prefs.getString("role", "Administrador");
        int userId = prefs.getInt("user_id", 0);

        // Mostrar los datos
        tvUsername.setText(username);
        tvRole.setText(role);
        tvDisplayUser.setText(username);
        tvDisplayRole.setText(role);
        tvDisplayID.setText("#" + String.format("%03d", userId));

        if (!username.isEmpty()) {
            tvInicial.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        SharedPreferences.Editor editor = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}