package com.julianramos.agrofarmmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumoAlimentosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ConsumoAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Consumo> consumoList;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumo_alimentos);

        Toolbar toolbar = findViewById(R.id.toolbarConsumos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consumo de Alimentos");
        }

        SharedPreferences prefs = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        recyclerView = findViewById(R.id.recyclerViewConsumos);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshConsumos);
        FloatingActionButton fab = findViewById(R.id.fabAgregarConsumo);

        consumoList = new ArrayList<>();
        adapter = new ConsumoAdapter(consumoList, token);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarConsumos);
        fab.setOnClickListener(v -> startActivity(new Intent(this, RegistrarConsumoActivity.class)));

        cargarConsumos();
    }

    private void cargarConsumos() {
        swipeRefreshLayout.setRefreshing(true);
        String url = "https://api-agrofarm.onrender.com/api/nutricion/consumos";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    consumoList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Consumo consumo = new Consumo(
                                    obj.optString("id", ""),
                                    obj.optString("pig_id", ""),
                                    obj.optString("alimento_id", ""),
                                    obj.optString("fecha", ""),
                                    obj.optDouble("cantidad_kg", 0.0),
                                    obj.optString("lote", ""),
                                    obj.optString("observaciones", ""),
                                    obj.optString("codigo_arete", "N/A"),
                                    obj.optString("nombre_alimento", "N/A")
                            );
                            consumoList.add(consumo);
                        }
                        adapter.updateList(consumoList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error al cargar consumos", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
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