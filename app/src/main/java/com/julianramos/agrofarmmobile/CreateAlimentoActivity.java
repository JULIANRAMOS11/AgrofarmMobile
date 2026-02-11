package com.julianramos.agrofarmmobile;

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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateAlimentoActivity extends AppCompatActivity {

    private static final String TAG = "CreateAlimentoActivity";
    private EditText etNombre, etProteina, etCosto, etProveedor, etStock;
    private TextInputLayout tilNombre, tilProteina, tilCosto, tilProveedor, tilStock;
    private Spinner spinnerTipo;
    private Button btnGuardar;
    private RequestQueue requestQueue;
    private boolean isEdit = false;
    private int alimentoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando formulario de alimento");
        setContentView(R.layout.activity_create_alimento);

        Toolbar toolbar = findViewById(R.id.toolbarCreateAlimento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombre = findViewById(R.id.etNombreAlimento);
        spinnerTipo = findViewById(R.id.spinnerTipoAlimento);
        etProteina = findViewById(R.id.etProteinaAlimento);
        etCosto = findViewById(R.id.etCostoAlimento);
        etProveedor = findViewById(R.id.etProveedorAlimento);
        etStock = findViewById(R.id.etStockAlimento);
        btnGuardar = findViewById(R.id.btnGuardarAlimento);

        // TextInputLayouts para mostrar errores
        tilNombre = (TextInputLayout) etNombre.getParent().getParent();
        tilProteina = (TextInputLayout) etProteina.getParent().getParent();
        tilCosto = (TextInputLayout) etCosto.getParent().getParent();
        tilProveedor = (TextInputLayout) etProveedor.getParent().getParent();
        tilStock = (TextInputLayout) etStock.getParent().getParent();

        requestQueue = Volley.newRequestQueue(this);

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            alimentoId = getIntent().getIntExtra("id", 0);
            etNombre.setText(getIntent().getStringExtra("nombre"));
            etProteina.setText(getIntent().getStringExtra("proteina"));
            etCosto.setText(getIntent().getStringExtra("costo"));
            etProveedor.setText(getIntent().getStringExtra("proveedor"));
            etStock.setText(getIntent().getStringExtra("stock"));
            btnGuardar.setText("ACTUALIZAR ALIMENTO");
        }

        btnGuardar.setOnClickListener(v -> {
            if (validateFields()) {
                saveAlimento();
            }
        });
    }

    private boolean validateFields() {
        boolean valid = true;
        tilNombre.setError(null);
        tilProteina.setError(null);
        tilCosto.setError(null);
        tilProveedor.setError(null);
        tilStock.setError(null);

        String nombre = etNombre.getText().toString().trim();
        String proteina = etProteina.getText().toString().trim();
        String costo = etCosto.getText().toString().trim();
        String proveedor = etProveedor.getText().toString().trim();
        String stock = etStock.getText().toString().trim();

        if (nombre.isEmpty()) { tilNombre.setError("Nombre requerido"); valid = false; }
        
        if (proteina.isEmpty()) { tilProteina.setError("Porcentaje requerido"); valid = false; }
        else {
            try {
                double p = Double.parseDouble(proteina);
                if (p < 0 || p > 100) { tilProteina.setError("Rango 0-100"); valid = false; }
            } catch (Exception e) { tilProteina.setError("Número inválido"); valid = false; }
        }

        if (costo.isEmpty()) { tilCosto.setError("Costo requerido"); valid = false; }
        else {
            try {
                if (Double.parseDouble(costo) <= 0) { tilCosto.setError("Debe ser > 0"); valid = false; }
            } catch (Exception e) { tilCosto.setError("Número inválido"); valid = false; }
        }

        if (proveedor.isEmpty()) { tilProveedor.setError("Proveedor requerido"); valid = false; }
        
        if (stock.isEmpty()) { tilStock.setError("Stock requerido"); valid = false; }
        else {
            try {
                if (Double.parseDouble(stock) < 0) { tilStock.setError("No puede ser negativo"); valid = false; }
            } catch (Exception e) { tilStock.setError("Número inválido"); valid = false; }
        }

        return valid;
    }

    private void saveAlimento() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nombre_alimento", etNombre.getText().toString().trim());
            jsonBody.put("tipo", spinnerTipo.getSelectedItem().toString());
            jsonBody.put("proteina_porcentaje", etProteina.getText().toString().trim());
            jsonBody.put("costo_por_kg", etCosto.getText().toString().trim());
            jsonBody.put("proveedor", etProveedor.getText().toString().trim());
            jsonBody.put("stock_kg", etStock.getText().toString().trim());
        } catch (JSONException e) { e.printStackTrace(); }

        String url = "https://api-agrofarm.onrender.com/api/nutricion/alimentos" + (isEdit ? "/" + alimentoId : "");
        int method = isEdit ? Request.Method.PUT : Request.Method.POST;

        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonBody,
                response -> {
                    Log.d(TAG, "onResponse: Alimento guardado correctamente");
                    Toast.makeText(this, "Operación exitosa", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    Log.e(TAG, "onErrorResponse: " + error.toString());
                    Toast.makeText(this, "Error al procesar: Reintenta", Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}