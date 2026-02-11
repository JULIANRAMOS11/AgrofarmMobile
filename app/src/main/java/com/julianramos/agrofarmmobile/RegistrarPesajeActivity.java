package com.julianramos.agrofarmmobile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrarPesajeActivity extends AppCompatActivity {

    private Spinner spinnerPig;
    private EditText etFecha, etPeso, etGanancia, etObservaciones;
    private Button btnRegistrar;
    private List<String> pigIds = new ArrayList<>();
    private List<String> pigAretes = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_pesaje);

        Toolbar toolbar = findViewById(R.id.toolbarRegistrarPesaje);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerPig = findViewById(R.id.spinnerPigPesaje);
        etFecha = findViewById(R.id.etFechaPesaje);
        etPeso = findViewById(R.id.etPesoKg);
        etGanancia = findViewById(R.id.etGananciaDiaria);
        etObservaciones = findViewById(R.id.etObservacionesPesaje);
        btnRegistrar = findViewById(R.id.btnRegistrarPesaje);

        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> showDatePicker());
        loadPigs();

        btnRegistrar.setOnClickListener(v -> registrarPesaje());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String m = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
            String d = day < 10 ? "0" + day : String.valueOf(day);
            etFecha.setText(year + "-" + m + "-" + d);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadPigs() {
        String url = "https://api-agrofarm.onrender.com/api/pigs";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            pigIds.add(String.valueOf(obj.getInt("id")));
                            pigAretes.add(obj.getString("codigo_arete"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pigAretes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPig.setAdapter(adapter);
                    } catch (JSONException e) { e.printStackTrace(); }
                }, null) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
                headers.put("Authorization", "Bearer " + prefs.getString("token", ""));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private void registrarPesaje() {
        if (pigIds.isEmpty() || etFecha.getText().toString().isEmpty() || etPeso.getText().toString().isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Registrando pesaje...");
        pd.show();

        JSONObject body = new JSONObject();
        try {
            body.put("pig_id", pigIds.get(spinnerPig.getSelectedItemPosition()));
            body.put("fecha_pesaje", etFecha.getText().toString());
            body.put("peso_kg", Double.parseDouble(etPeso.getText().toString()));
            String ganancia = etGanancia.getText().toString();
            if (!ganancia.isEmpty()) body.put("ganancia_diaria", Double.parseDouble(ganancia));
            body.put("observaciones", etObservaciones.getText().toString());
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://api-agrofarm.onrender.com/api/produccion", body,
                response -> {
                    pd.dismiss();
                    Toast.makeText(this, "Pesaje registrado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                }, error -> {
                    pd.dismiss();
                    Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
                headers.put("Authorization", "Bearer " + prefs.getString("token", ""));
                return headers;
            }
        };
        requestQueue.add(request);
    }
}