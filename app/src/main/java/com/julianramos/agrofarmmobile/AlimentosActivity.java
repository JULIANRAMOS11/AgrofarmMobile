package com.julianramos.agrofarmmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlimentosActivity extends AppCompatActivity {

    private static final String TAG = "AlimentosActivity";
    private RecyclerView rvAlimentos;
    private AlimentoAdapter adapter;
    private List<Alimento> alimentoList;
    private LinearLayout layoutLoader;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private RequestQueue requestQueue;

    // Agregamos la variable para leer la caja fuerte
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Abriendo AlimentosActivity");
        setContentView(R.layout.activity_alimentos);

        Toolbar toolbar = findViewById(R.id.toolbarAlimentos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Inicializamos la caja fuerte de AgroFarm
        sharedPreferences = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);

        rvAlimentos = findViewById(R.id.rvAlimentos);
        layoutLoader = findViewById(R.id.layoutLoaderAlimentos);
        tvEmpty = findViewById(R.id.tvEmptyAlimentos);
        swipeRefresh = findViewById(R.id.swipeRefreshAlimentos);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddAlimento);

        rvAlimentos.setLayoutManager(new LinearLayoutManager(this));
        alimentoList = new ArrayList<>();
        adapter = new AlimentoAdapter(alimentoList);
        rvAlimentos.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        swipeRefresh.setOnRefreshListener(this::fetchAlimentos);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, CreateAlimentoActivity.class)));

        fetchAlimentos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar alimento...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        return true;
    }

    private void fetchAlimentos() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            handleError("Sin conexión a internet.");
            swipeRefresh.setRefreshing(false);
            return;
        }

        String url = "https://api-agrofarm.onrender.com/api/nutricion/alimentos";
        if (!swipeRefresh.isRefreshing()) layoutLoader.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Extraemos la llave de seguridad para que el servidor nos deje entrar
        String token = sharedPreferences.getString("token", "");

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "onResponse: Alimentos cargados: " + response.length());
                    layoutLoader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    alimentoList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            alimentoList.add(new Alimento(
                                    obj.getInt("id"),
                                    obj.getString("nombre_alimento"),
                                    obj.getString("tipo"),
                                    obj.getString("proteina_porcentaje"),
                                    obj.getString("costo_por_kg"),
                                    obj.getString("proveedor"),
                                    obj.getString("stock_kg")
                            ));
                        }
                        adapter.updateList(alimentoList);
                        if (alimentoList.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Log.e(TAG, "ParseError: " + e.getMessage());
                    }
                },
                error -> {
                    layoutLoader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    handleVolleyError(error);
                }) {
            // AQUÍ ESTÁ LA MAGIA: Le enviamos el token al servidor
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void handleVolleyError(com.android.volley.VolleyError error) {
        Log.e(TAG, "Error: " + error.toString());
        if (error instanceof TimeoutError) handleError("El servidor tardó en responder.");
        else if (error instanceof NoConnectionError) handleError("Sin conexión a internet.");
        else handleError("Error del servidor.");
    }

    private void handleError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}