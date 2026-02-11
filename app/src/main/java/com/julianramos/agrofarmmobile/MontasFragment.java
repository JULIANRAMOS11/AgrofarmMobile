package com.julianramos.agrofarmmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class MontasFragment extends Fragment {

    private MontaAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Monta> montaList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_montas, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMontas);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshMontas);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregarMonta);

        montaList = new ArrayList<>();
        adapter = new MontaAdapter(montaList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarMontas);
        fab.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), RegistrarMontaActivity.class));
            }
        });

        cargarMontas();

        return view;
    }

    public void cargarMontas() {
        if (!hayInternet()) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        String url = "https://api-agrofarm.onrender.com/api/reproduccion";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    montaList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            // Usar el constructor con argumentos ya que no hay setters
                            Monta monta = new Monta(
                                    obj.optString("id", ""),
                                    obj.optString("pig_id", ""),
                                    obj.optString("fecha_servicio", ""),
                                    obj.optString("tipo_servicio", ""),
                                    obj.optString("observaciones", ""),
                                    obj.optString("codigo_arete", "N/A")
                            );
                            montaList.add(monta);
                        }
                        adapter.updateList(montaList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar montas", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (getActivity() != null) {
                    SharedPreferences prefs = getActivity().getSharedPreferences("AgrofarmPrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("token", "");
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        if (getContext() != null) {
            Volley.newRequestQueue(getContext()).add(request);
        }
    }

    private boolean hayInternet() {
        if (getActivity() == null) return false;
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    
    public void filtrar(String text) {
        if (adapter != null) {
            adapter.filter(text);
        }
    }
}