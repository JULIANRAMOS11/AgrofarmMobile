package com.julianramos.agrofarmmobile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class CreatePigActivity extends AppCompatActivity {

    private EditText etCodigoArete, etFechaNacimiento, etPesoActual;
    private Spinner spinnerSexo, spinnerEstado;
    private Button btnGuardar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pig);

        Toolbar toolbar = findViewById(R.id.toolbarCreatePig);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etCodigoArete = findViewById(R.id.etCodigoArete);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etPesoActual = findViewById(R.id.etPesoActual);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);

        requestQueue = Volley.newRequestQueue(this);

        etFechaNacimiento.setOnClickListener(v -> showDatePicker());

        btnGuardar.setOnClickListener(v -> savePig());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String monthStr = (monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);
                    String dayStr = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                    etFechaNacimiento.setText(year1 + "-" + monthStr + "-" + dayStr);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void savePig() {
        String codigo = etCodigoArete.getText().toString().trim();
        String fecha = etFechaNacimiento.getText().toString().trim();
        String peso = etPesoActual.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();

        if (codigo.isEmpty() || fecha.isEmpty() || peso.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api-agrofarm.onrender.com/api/pigs";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("codigo_arete", codigo);
            jsonBody.put("sexo", sexo);
            jsonBody.put("fecha_nacimiento", fecha);
            jsonBody.put("peso_actual", peso);
            jsonBody.put("estado", estado);
            jsonBody.put("etapa", "CEBA"); // Valor por defecto según requerimiento
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("GUARDANDO...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(CreatePigActivity.this, "Cerdo registrado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("GUARDAR REGISTRO");
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(CreatePigActivity.this, "Error al guardar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                35000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }
}