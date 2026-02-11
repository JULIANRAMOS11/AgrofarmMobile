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

public class CreateSanidadActivity extends AppCompatActivity {

    private static final String TAG = "CreateSanidadActivity";
    private Spinner spinnerPig, spinnerTipo;
    private EditText etFecha, etDescripcion, etVeterinario, etDosis, etObservaciones;
    private TextInputLayout tilFecha, tilDescripcion, tilVeterinario, tilDosis;
    private Button btnGuardar;
    private RequestQueue requestQueue;
    private List<Integer> pigIds = new ArrayList<>();
    private List<String> pigNames = new ArrayList<>();
    private boolean isEdit = false;
    private int sanidadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Abriendo formulario sanidad");
        setContentView(R.layout.activity_create_sanidad);

        Toolbar toolbar = findViewById(R.id.toolbarCreateSanidad);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerPig = findViewById(R.id.spinnerPigSanidad);
        spinnerTipo = findViewById(R.id.spinnerTipoTratamiento);
        etFecha = findViewById(R.id.etFechaSanidad);
        etDescripcion = findViewById(R.id.etDescripcionSanidad);
        etVeterinario = findViewById(R.id.etVeterinarioSanidad);
        etDosis = findViewById(R.id.etDosisSanidad);
        etObservaciones = findViewById(R.id.etObservacionesSanidad);
        btnGuardar = findViewById(R.id.btnGuardarSanidad);
        
        // TextInputLayouts para errores
        tilFecha = (TextInputLayout) etFecha.getParent().getParent();
        tilDescripcion = (TextInputLayout) etDescripcion.getParent().getParent();
        tilVeterinario = (TextInputLayout) etVeterinario.getParent().getParent();
        tilDosis = (TextInputLayout) etDosis.getParent().getParent();

        requestQueue = Volley.newRequestQueue(this);

        etFecha.setOnClickListener(v -> showDatePicker());
        loadPigs();

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            sanidadId = getIntent().getIntExtra("id", 0);
            etFecha.setText(getIntent().getStringExtra("fecha"));
            etDescripcion.setText(getIntent().getStringExtra("descripcion"));
            etVeterinario.setText(getIntent().getStringExtra("veterinario"));
            etDosis.setText(getIntent().getStringExtra("dosis"));
            etObservaciones.setText(getIntent().getStringExtra("observaciones"));
            btnGuardar.setText("ACTUALIZAR REGISTRO");
        }

        btnGuardar.setOnClickListener(v -> {
            if (validateFields()) {
                saveSanidad();
            }
        });
    }

    private boolean validateFields() {
        boolean valid = true;
        tilFecha.setError(null);
        tilDescripcion.setError(null);
        tilVeterinario.setError(null);
        tilDosis.setError(null);

        String fecha = etFecha.getText().toString().trim();
        String desc = etDescripcion.getText().toString().trim();
        String vet = etVeterinario.getText().toString().trim();
        String dosis = etDosis.getText().toString().trim();

        if (fecha.isEmpty()) { tilFecha.setError("Fecha requerida"); valid = false; }
        else if (isFutureDate(fecha)) { tilFecha.setError("No se permiten fechas futuras"); valid = false; }
        
        if (desc.isEmpty()) { tilDescripcion.setError("Descripción requerida"); valid = false; }
        if (vet.isEmpty()) { tilVeterinario.setError("Veterinario requerido"); valid = false; }
        if (dosis.isEmpty()) { tilDosis.setError("Dosis requerida"); valid = false; }

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

    private void saveSanidad() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pigIds.isEmpty()) {
            Toast.makeText(this, "No hay cerdos cargados", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        int pig_id = pigIds.get(spinnerPig.getSelectedItemPosition());
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("pig_id", pig_id);
            jsonBody.put("fecha", etFecha.getText().toString());
            jsonBody.put("tipo_tratamiento", spinnerTipo.getSelectedItem().toString());
            jsonBody.put("descripcion", etDescripcion.getText().toString());
            jsonBody.put("veterinario", etVeterinario.getText().toString());
            jsonBody.put("dosis", etDosis.getText().toString());
            jsonBody.put("observaciones", etObservaciones.getText().toString());
        } catch (JSONException e) { e.printStackTrace(); }

        String url = "https://api-agrofarm.onrender.com/api/sanidad" + (isEdit ? "/" + sanidadId : "");
        int method = isEdit ? Request.Method.PUT : Request.Method.POST;

        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonBody,
                response -> {
                    Log.d(TAG, "onResponse: Guardado exitoso");
                    Toast.makeText(this, "Registro guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    Log.e(TAG, "onErrorResponse: " + error.toString());
                    Toast.makeText(this, "Error al guardar: Reintenta", Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}