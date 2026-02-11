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

public class PesajesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PesajeAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Pesaje> pesajeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesajes);

        Toolbar toolbar = findViewById(R.id.toolbarPesajes);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Control de Pesajes");
        }

        recyclerView = findViewById(R.id.recyclerViewPesajes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshPesajes);
        FloatingActionButton fab = findViewById(R.id.fabAgregarPesaje);

        pesajeList = new ArrayList<>();
        adapter = new PesajeAdapter(pesajeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarPesajes);
        fab.setOnClickListener(v -> startActivity(new Intent(this, RegistrarPesajeActivity.class)));

        cargarPesajes();
    }

    private void cargarPesajes() {
        swipeRefreshLayout.setRefreshing(true);
        String url = "https://api-agrofarm.onrender.com/api/produccion";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    pesajeList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Pesaje pesaje = new Pesaje(
                                    obj.optString("id", ""),
                                    obj.optString("pig_id", ""),
                                    obj.optString("fecha_pesaje", ""),
                                    obj.optDouble("peso_kg", 0.0),
                                    obj.optDouble("ganancia_diaria", 0.0),
                                    obj.optString("observaciones", ""),
                                    obj.optString("codigo_arete", "N/A")
                            );
                            pesajeList.add(pesaje);
                        }
                        adapter.updateList(pesajeList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error al cargar pesajes", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
                headers.put("Authorization", "Bearer " + prefs.getString("token", ""));
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