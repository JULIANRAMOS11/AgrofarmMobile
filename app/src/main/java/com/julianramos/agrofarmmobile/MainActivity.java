package com.julianramos.agrofarmmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private CheckBox cbRecordarme;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AgrofarmPrefs", MODE_PRIVATE);
        
        if (sharedPreferences.getBoolean("rememberMe", false) && sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etEmail); 
        etPassword = findViewById(R.id.etPassword);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        Button btnIngresar = findViewById(R.id.btnIngresar);
        requestQueue = Volley.newRequestQueue(this);

        btnIngresar.setOnClickListener(v -> login());
    }

    private void login() {
        // Obtenemos el texto y lo pasamos a MAYÚSCULAS para que coincida con tu base de datos
        // sin importar cómo lo escriba el usuario
        String userInput = etUsername.getText().toString().trim().toUpperCase();
        String password = etPassword.getText().toString().trim();

        if (userInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api-agrofarm.onrender.com/api/auth/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", userInput);
            jsonBody.put("password", password);
            Log.d(TAG, "Enviando login (Caps): " + userInput);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        JSONObject usuario = response.has("usuario") ? response.getJSONObject("usuario") : response;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("user_id", usuario.optInt("id", 0));
                        editor.putString("username", usuario.optString("username", userInput));
                        editor.putString("role", usuario.optString("role", "Usuario")); // Re-añadido role
                        editor.putBoolean("isLoggedIn", true);
                        editor.putBoolean("rememberMe", cbRecordarme.isChecked());
                        editor.apply();

                        Toast.makeText(MainActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    } catch (JSONException e) { 
                        Log.e(TAG, "JSON Error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error de respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(35000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}