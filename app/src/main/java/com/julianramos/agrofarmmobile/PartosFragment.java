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

public class PartosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PartoAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Parto> partoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_partos, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPartos);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshPartos);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregarParto);

        partoList = new ArrayList<>();
        adapter = new PartoAdapter(partoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::cargarPartos);
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegistrarPartoActivity.class)));

        cargarPartos();

        return view;
    }

    public void cargarPartos() {
        if (!hayInternet()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        swipeRefreshLayout.setRefreshing(true);
        String url = "https://api-agrofarm.onrender.com/api/reproduccion/partos/all";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    partoList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Parto parto = new Parto(
                                    obj.getString("id"),
                                    obj.getString("pig_id"),
                                    obj.getString("fecha_parto"),
                                    obj.getInt("lechones_vivos"),
                                    obj.getInt("lechones_muertos"),
                                    obj.getDouble("peso_promedio_lechon"),
                                    obj.optString("observaciones", ""),
                                    obj.optString("codigo_arete", "N/A")
                            );
                            partoList.add(parto);
                        }
                        adapter.updateList(partoList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error al cargar partos", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getActivity().getSharedPreferences("AgrofarmPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }

    private boolean hayInternet() {
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