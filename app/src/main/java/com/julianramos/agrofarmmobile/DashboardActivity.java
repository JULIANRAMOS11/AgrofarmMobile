package com.julianramos.agrofarmmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SharedPreferences sharedPreferences;
    private TextView textTotalCerdos, textCerdosActivos, textPesoPromedio, textStockAlimentos;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        sharedPreferences = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        setupHeader(navigationView);

        textTotalCerdos = findViewById(R.id.textTotalCerdos);
        textCerdosActivos = findViewById(R.id.textCerdosActivos);
        textPesoPromedio = findViewById(R.id.textPesoPromedio);
        textStockAlimentos = findViewById(R.id.textStockAlimentos);

        // Click listeners para las tarjetas del Dashboard
        findViewById(R.id.cardCerdos).setOnClickListener(v -> startActivity(new Intent(this, PigsActivity.class)));
        findViewById(R.id.cardSanidad).setOnClickListener(v -> startActivity(new Intent(this, SanidadActivity.class)));
        findViewById(R.id.cardAlimentos).setOnClickListener(v -> startActivity(new Intent(this, AlimentosActivity.class)));
        findViewById(R.id.cardReportes).setOnClickListener(v -> startActivity(new Intent(this, ProduccionActivity.class)));

        cargarEstadisticas();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    DashboardActivity.super.onBackPressed();
                }
            }
        });
    }

    private void cargarEstadisticas() {
        String token = sharedPreferences.getString("token", "");

        // Cerdos stats
        JsonArrayRequest requestCerdos = new JsonArrayRequest(Request.Method.GET, "https://api-agrofarm.onrender.com/api/pigs", null,
                response -> {
                    try {
                        textTotalCerdos.setText(String.valueOf(response.length()));
                        int activos = 0;
                        double sumaPesos = 0;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject cerdo = response.getJSONObject(i);
                            if ("ACTIVO".equalsIgnoreCase(cerdo.optString("estado", ""))) activos++;
                            sumaPesos += cerdo.optDouble("peso_actual", 0);
                        }
                        textCerdosActivos.setText(String.valueOf(activos));
                        double promedio = response.length() > 0 ? sumaPesos / response.length() : 0;
                        textPesoPromedio.setText(String.format(Locale.getDefault(), "%.1f kg", promedio));
                    } catch (JSONException e) { e.printStackTrace(); }
                }, null) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(requestCerdos);

        // Alimentos stats
        JsonArrayRequest requestAlimentos = new JsonArrayRequest(Request.Method.GET, "https://api-agrofarm.onrender.com/api/nutricion/alimentos", null,
                response -> {
                    try {
                        double totalStock = 0;
                        for (int i = 0; i < response.length(); i++) {
                            totalStock += response.getJSONObject(i).getDouble("stock_kg");
                        }
                        textStockAlimentos.setText(String.format(Locale.getDefault(), "%.0f kg", totalStock));
                    } catch (JSONException e) { e.printStackTrace(); }
                }, null) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(requestAlimentos);
    }

    private void setupHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tvNavUsername);
        TextView tvRole = headerView.findViewById(R.id.tvNavRole);
        tvUsername.setText(sharedPreferences.getString("username", "Usuario"));
        tvRole.setText(sharedPreferences.getString("role", "Administrador"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            // Ya estamos aquí
        } else if (id == R.id.nav_pigs) {
            startActivity(new Intent(this, PigsActivity.class));
        } else if (id == R.id.nav_reproduccion) {
            startActivity(new Intent(this, ReproduccionActivity.class));
        } else if (id == R.id.nav_pesajes) {
            startActivity(new Intent(this, PesajesActivity.class));
        } else if (id == R.id.nav_consumos) {
            startActivity(new Intent(this, ConsumoAlimentosActivity.class));
        } else if (id == R.id.nav_sanidad) {
            startActivity(new Intent(this, SanidadActivity.class));
        } else if (id == R.id.nav_alimentos) {
            startActivity(new Intent(this, AlimentosActivity.class));
        } else if (id == R.id.nav_perfil) {
            startActivity(new Intent(this, PerfilActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}