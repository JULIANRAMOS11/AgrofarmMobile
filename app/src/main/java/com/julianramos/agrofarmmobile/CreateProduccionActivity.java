package com.julianramos.agrofarmmobile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateProduccionActivity extends AppCompatActivity {

    private static final String TAG = "CreateProduccionActivity";
    private Spinner spinnerPig;
    private EditText etFecha, etPeso, etEdad, etGanancia, etConsumo, etConversion;
    private TextInputLayout tilFecha, tilPeso, tilEdad, tilGanancia, tilConsumo, tilConversion;
    private Button btnGuardar;
    private RequestQueue requestQueue;
    private List<Integer> pigIds = new ArrayList<>();
    private List<String> pigNames = new ArrayList<>();
    private boolean isEdit = false;
    private int produccionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Abriendo formulario producción");
        setContentView(R.layout.activity_create_produccion);

        Toolbar toolbar = findViewById(R.id.toolbarCreateProduccion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerPig = findViewById(R.id.spinnerPigProduccion);
        etFecha = findViewById(R.id.etFechaProduccion);
        etPeso = findViewById(R.id.etPesoProduccion);
        etEdad = findViewById(R.id.etEdadProduccion);
        etGanancia = findViewById(R.id.etGananciaProduccion);
        etConsumo = findViewById(R.id.etConsumoProduccion);
        etConversion = findViewById(R.id.etConversionProduccion);
        btnGuardar = findViewById(R.id.btnGuardarProduccion);

        tilFecha = (TextInputLayout) etFecha.getParent().getParent();
        tilPeso = (TextInputLayout) etPeso.getParent().getParent();
        tilEdad = (TextInputLayout) etEdad.getParent().getParent();
        tilGanancia = (TextInputLayout) etGanancia.getParent().getParent();
        tilConsumo = (TextInputLayout) etConsumo.getParent().getParent();
        tilConversion = (TextInputLayout) etConversion.getParent().getParent();

        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> showDatePicker());
        loadPigs();

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            produccionId = getIntent().getIntExtra("id", 0);
            etFecha.setText(getIntent().getStringExtra("fecha"));
            etPeso.setText(getIntent().getStringExtra("peso"));
            etEdad.setText(String.valueOf(getIntent().getIntExtra("edad_dias", 0)));
            etGanancia.setText(getIntent().getStringExtra("ganancia"));
            etConsumo.setText(getIntent().getStringExtra("consumo"));
            etConversion.setText(getIntent().getStringExtra("conversion"));
            btnGuardar.setText("ACTUALIZAR REPORTE");
        }

        btnGuardar.setOnClickListener(v -> {
            if (validateFields()) {
                saveProduccion();
            }
        });
    }

    private boolean validateFields() {
        boolean valid = true;
        tilFecha.setError(null);
        tilPeso.setError(null);
        tilEdad.setError(null);
        tilGanancia.setError(null);
        tilConsumo.setError(null);
        tilConversion.setError(null);

        if (etFecha.getText().toString().isEmpty()) { tilFecha.setError("Fecha requerida"); valid = false; }
        else if (isFutureDate(etFecha.getText().toString())) { tilFecha.setError("No fechas futuras"); valid = false; }

        if (etPeso.getText().toString().isEmpty()) { tilPeso.setError("Peso requerido"); valid = false; }
        else if (Double.parseDouble(etPeso.getText().toString()) <= 0) { tilPeso.setError("Debe ser > 0"); valid = false; }

        if (etEdad.getText().toString().isEmpty()) { tilEdad.setError("Edad requerida"); valid = false; }
        if (etGanancia.getText().toString().isEmpty()) { tilGanancia.setError("Campo requerido"); valid = false; }
        if (etConsumo.getText().toString().isEmpty()) { tilConsumo.setError("Campo requerido"); valid = false; }
        if (etConversion.getText().toString().isEmpty()) { tilConversion.setError("Campo requerido"); valid = false; }

        return valid;
    }

    private boolean isFutureDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            return date != null && date.after(new Date());
        } catch (ParseException e) { return false; }
    }

    private void loadPigs() {
        String url = "https://api-agrofarm.onrender.com/api/pigs";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            pigIds.add(obj.getInt("id"));
                            pigNames.add(obj.getString("codigo_arete"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pigNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPig.setAdapter(adapter);
                        if (isEdit) {
                            int currentPigId = getIntent().getIntExtra("pig_id", 0);
                            int index = pigIds.indexOf(currentPigId);
                            if (index >= 0) spinnerPig.setSelection(index);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }, null);
        requestQueue.add(request);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String m = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
            String d = day < 10 ? "0" + day : String.valueOf(day);
            etFecha.setText(year + "-" + m + "-" + d);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProduccion() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión", Toast.LENGTH_SHORT).show();
            return;
        }
        btnGuardar.setEnabled(false);
        int pig_id = pigIds.get(spinnerPig.getSelectedItemPosition());
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("pig_id", pig_id);
            jsonBody.put("fecha", etFecha.getText().toString());
            jsonBody.put("peso", etPeso.getText().toString());
            jsonBody.put("edad_dias", Integer.parseInt(etEdad.getText().toString()));
            jsonBody.put("ganancia_diaria", etGanancia.getText().toString());
            jsonBody.put("consumo_alimento_kg", etConsumo.getText().toString());
            jsonBody.put("conversion_alimenticia", etConversion.getText().toString());
        } catch (JSONException | NumberFormatException e) { e.printStackTrace(); }

        String url = "https://api-agrofarm.onrender.com/api/produccion" + (isEdit ? "/" + produccionId : "");
        int method = isEdit ? Request.Method.PUT : Request.Method.POST;

        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Éxito", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error: Reintenta", Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}