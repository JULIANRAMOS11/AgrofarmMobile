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

public class EditPigActivity extends AppCompatActivity {

    private EditText etCodigoArete, etFechaNacimiento, etPesoActual;
    private Spinner spinnerSexo, spinnerEstado;
    private Button btnGuardar;
    private RequestQueue requestQueue;
    private int pigId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pig); // Reutilizamos el layout de creación

        Toolbar toolbar = findViewById(R.id.toolbarCreatePig);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar Cerdo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etCodigoArete = findViewById(R.id.etCodigoArete);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etPesoActual = findViewById(R.id.etPesoActual);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setText("ACTUALIZAR CAMBIOS");

        requestQueue = Volley.newRequestQueue(this);

        // Obtener datos del intent
        if (getIntent().hasExtra("id")) {
            pigId = getIntent().getIntExtra("id", 0);
            etCodigoArete.setText(getIntent().getStringExtra("codigo_arete"));
            etFechaNacimiento.setText(getIntent().getStringExtra("fecha_nacimiento"));
            etPesoActual.setText(getIntent().getStringExtra("peso_actual"));
            
            // Set spinners (simplificado)
            String sexo = getIntent().getStringExtra("sexo");
            if ("Macho".equals(sexo)) spinnerSexo.setSelection(0); else spinnerSexo.setSelection(1);
            
            String estado = getIntent().getStringExtra("estado");
            if ("ACTIVO".equalsIgnoreCase(estado)) spinnerEstado.setSelection(0); else spinnerEstado.setSelection(1);
        }

        etFechaNacimiento.setOnClickListener(v -> showDatePicker());
        btnGuardar.setOnClickListener(v -> updatePig());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String monthStr = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
                    String dayStr = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                    etFechaNacimiento.setText(year + "-" + monthStr + "-" + dayStr);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updatePig() {
        String url = "https://api-agrofarm.onrender.com/api/pigs/" + pigId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("codigo_arete", etCodigoArete.getText().toString());
            jsonBody.put("sexo", spinnerSexo.getSelectedItem().toString());
            jsonBody.put("fecha_nacimiento", etFechaNacimiento.getText().toString());
            jsonBody.put("peso_actual", etPesoActual.getText().toString());
            jsonBody.put("estado", spinnerEstado.getSelectedItem().toString());
            jsonBody.put("etapa", "CEBA");
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}