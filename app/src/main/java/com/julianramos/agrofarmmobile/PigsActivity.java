package com.julianramos.agrofarmmobile;

import android.content.Intent;
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
import java.util.List;

public class PigsActivity extends AppCompatActivity {

    private static final String TAG = "PigsActivity";
    private RecyclerView rvPigs;
    private PigAdapter adapter;
    private List<Pig> pigList;
    private LinearLayout layoutLoader;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando PigsActivity");
        setContentView(R.layout.activity_pigs);

        Toolbar toolbar = findViewById(R.id.toolbarPigs);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvPigs = findViewById(R.id.rvPigs);
        layoutLoader = findViewById(R.id.layoutLoader);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        FloatingActionButton fabAddPig = findViewById(R.id.fabAddPig);
        
        rvPigs.setLayoutManager(new LinearLayoutManager(this));
        pigList = new ArrayList<>();
        adapter = new PigAdapter(pigList);
        rvPigs.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        swipeRefresh.setOnRefreshListener(this::fetchPigs);
        fabAddPig.setOnClickListener(v -> startActivity(new Intent(this, CreatePigActivity.class)));

        fetchPigs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
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

    private void fetchPigs() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("Sin conexión a internet. Verifica tu red.");
            swipeRefresh.setRefreshing(false);
            layoutLoader.setVisibility(View.GONE);
            return;
        }

        String url = "https://api-agrofarm.onrender.com/api/pigs";
        if (!swipeRefresh.isRefreshing()) layoutLoader.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "onResponse: Datos recibidos: " + response.length() + " cerdos");
                    layoutLoader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    pigList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            pigList.add(new Pig(
                                    obj.optInt("id", 0),
                                    obj.optString("codigo_arete", "S/N"),
                                    obj.optString("sexo", "Macho"),
                                    obj.optString("fecha_nacimiento", ""),
                                    obj.optString("estado", "ACTIVO"),
                                    obj.optString("peso_actual", "0"),
                                    obj.optString("lote_nombre", obj.optString("lote", "Sin Lote")),
                                    obj.optString("etapa_nombre", obj.optString("etapa", "Sin Etapa")),
                                    obj.optString("created_at", "")
                            ));
                        }
                        adapter.updateList(pigList);
                        if (pigList.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Log.e(TAG, "ParseError: " + e.getMessage());
                    }
                },
                error -> {
                    layoutLoader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    handleVolleyError(error);
                });

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void handleVolleyError(com.android.volley.VolleyError error) {
        Log.e(TAG, "onErrorResponse: " + error.toString());
        if (error instanceof TimeoutError) {
            showError("El servidor tardó mucho en responder. Reintenta.");
        } else if (error instanceof NoConnectionError) {
            showError("No hay conexión a internet.");
        } else if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
            showError("Error interno del servidor (500).");
        } else {
            showError("Error al conectar con el servidor.");
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}