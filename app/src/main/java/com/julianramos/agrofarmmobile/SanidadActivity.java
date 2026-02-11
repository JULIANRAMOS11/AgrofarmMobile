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

public class SanidadActivity extends AppCompatActivity {

    private static final String TAG = "SanidadActivity";
    private RecyclerView rvSanidad;
    private SanidadAdapter adapter;
    private List<Sanidad> sanidadList;
    private LinearLayout layoutLoader;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Abriendo SanidadActivity");
        setContentView(R.layout.activity_sanidad);

        Toolbar toolbar = findViewById(R.id.toolbarSanidad);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvSanidad = findViewById(R.id.rvSanidad);
        layoutLoader = findViewById(R.id.layoutLoaderSanidad);
        tvEmpty = findViewById(R.id.tvEmptySanidad);
        swipeRefresh = findViewById(R.id.swipeRefreshSanidad);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddSanidad);

        rvSanidad.setLayoutManager(new LinearLayoutManager(this));
        sanidadList = new ArrayList<>();
        adapter = new SanidadAdapter(sanidadList);
        rvSanidad.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        swipeRefresh.setOnRefreshListener(this::fetchSanidad);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, CreateSanidadActivity.class)));

        fetchSanidad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar por tratamiento...");
        
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

    private void fetchSanidad() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            handleError("Sin conexión a internet.");
            swipeRefresh.setRefreshing(false);
            return;
        }

        String url = "https://api-agrofarm.onrender.com/api/sanidad";
        if (!swipeRefresh.isRefreshing()) layoutLoader.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "onResponse: Registros recibidos: " + response.length());
                    layoutLoader.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    sanidadList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            sanidadList.add(new Sanidad(
                                    obj.getInt("id"),
                                    obj.optInt("pig_id", 0),
                                    obj.optString("fecha", ""),
                                    obj.optString("tipo_tratamiento", "N/A"),
                                    obj.optString("descripcion", ""),
                                    obj.optString("veterinario", "No especificado"),
                                    obj.optString("dosis", ""),
                                    obj.optString("observaciones", "")
                            ));
                        }
                        adapter.updateList(sanidadList);
                        if (sanidadList.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
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