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

        // ESCUDO ANTIFALLOS: Comprobamos recordarme, login Y que exista el token
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String savedToken = sharedPreferences.getString("token", "");

        if (rememberMe && isLoggedIn && !savedToken.isEmpty()) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        } else if (isLoggedIn && savedToken.isEmpty()) {
            sharedPreferences.edit().clear().apply();
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
        // Obtenemos el texto respetando mayúsculas y minúsculas originales
        String userInput = etUsername.getText().toString().trim();
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
            Log.d(TAG, "Enviando login: " + userInput);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        // ATRAPAMOS EL TOKEN DEL SERVIDOR
                        String token = response.optString("token", "");

                        JSONObject usuario = response.has("usuario") ? response.getJSONObject("usuario") : response;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("user_id", usuario.optInt("id", 0));
                        editor.putString("username", usuario.optString("username", userInput));
                        editor.putString("role", usuario.optString("role", "Usuario"));

                        // GUARDAMOS EL TOKEN EN LA CAJA FUERTE
                        editor.putString("token", token);

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