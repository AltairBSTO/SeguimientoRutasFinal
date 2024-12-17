package com.bsto.seguimientorutas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Inicializar los campos del formulario de login
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Verificar si ya hay un usuario logueado
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Si el usuario ya está logueado, redirigirlo al mapa
            goToMapActivity();
        }

        // Configurar el clic para iniciar sesión con correo y contraseña
        TextView loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> loginUserWithEmailPassword());

        // Configurar el clic para redirigir a RegistroApp
        TextView registerTextView = findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(v -> {
            // Crea un Intent para abrir la actividad de registro
            Intent intent = new Intent(MainActivity.this, registroApp.class);
            startActivity(intent);
        });
    }

    // Método para manejar el inicio de sesión
    private void loginUserWithEmailPassword() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Correo electrónico requerido");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Contraseña requerida");
            return;
        }

        // Iniciar sesión con Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Si la autenticación es exitosa, redirigir al mapa
                        goToMapActivity();
                    } else {
                        // Si ocurre un error, mostrar un mensaje
                        Toast.makeText(MainActivity.this, "Error de inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Método para redirigir a la actividad del mapa
    private void goToMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class); // Suponiendo que la actividad del mapa se llama MapActivity
        startActivity(intent);
        finish(); // Cerrar la actividad de login para que no se pueda volver a ella
    }
}
