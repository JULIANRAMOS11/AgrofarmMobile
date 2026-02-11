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

public class RegistrarPartoActivity extends AppCompatActivity {

    private Spinner spinnerCerda;
    private EditText etFecha, etVivos, etMuertos, etPeso, etObservaciones;
    private Button btnRegistrar;
    private List<String> cerdaIds = new ArrayList<>();
    private List<String> cerdaAretes = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_parto);

        Toolbar toolbar = findViewById(R.id.toolbarRegistrarParto);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerCerda = findViewById(R.id.spinnerCerdaParto);
        etFecha = findViewById(R.id.etFechaParto);
        etVivos = findViewById(R.id.etLechonesVivos);
        etMuertos = findViewById(R.id.etLechonesMuertos);
        etPeso = findViewById(R.id.etPesoPromedioLechon);
        etObservaciones = findViewById(R.id.etObservacionesParto);
        btnRegistrar = findViewById(R.id.btnRegistrarParto);

        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> showDatePicker());
        loadCerdas();

        btnRegistrar.setOnClickListener(v -> registrarParto());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String m = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
            String d = day < 10 ? "0" + day : String.valueOf(day);
            etFecha.setText(year + "-" + m + "-" + d);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadCerdas() {
        String url = "https://api-agrofarm.onrender.com/api/pigs";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            if (obj.getString("sexo").equalsIgnoreCase("Hembra") || obj.getString("sexo").equalsIgnoreCase("H")) {
                                cerdaIds.add(String.valueOf(obj.getInt("id")));
                                cerdaAretes.add(obj.getString("codigo_arete"));
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cerdaAretes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCerda.setAdapter(adapter);
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

    private void registrarParto() {
        if (cerdaIds.isEmpty() || etFecha.getText().toString().isEmpty() || etVivos.getText().toString().isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Registrando parto...");
        pd.show();

        JSONObject body = new JSONObject();
        try {
            body.put("pig_id", cerdaIds.get(spinnerCerda.getSelectedItemPosition()));
            body.put("fecha_parto", etFecha.getText().toString());
            body.put("lechones_vivos", Integer.parseInt(etVivos.getText().toString()));
            body.put("lechones_muertos", etMuertos.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMuertos.getText().toString()));
            body.put("peso_promedio_lechon", etPeso.getText().toString().isEmpty() ? 0 : Double.parseDouble(etPeso.getText().toString()));
            body.put("observaciones", etObservaciones.getText().toString());
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://api-agrofarm.onrender.com/api/reproduccion/partos", body,
                response -> {
                    pd.dismiss();
                    Toast.makeText(this, "Parto registrado exitosamente", Toast.LENGTH_SHORT).show();
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