package com.bsto.seguimientorutas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class registroApp extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, usernameEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // TextViews para mostrar los requisitos de la contraseña
    private TextView uppercaseTextView, numberTextView, specialCharTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_app);

        // Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        usernameEditText = findViewById(R.id.nameEditText);
        registerButton = findViewById(R.id.registerButton);

        // Inicializar los TextViews para los requisitos de la contraseña
        uppercaseTextView = findViewById(R.id.uppercaseTextView);
        numberTextView = findViewById(R.id.numberTextView);
        specialCharTextView = findViewById(R.id.specialCharTextView);

        // Configurar el botón de registro
        registerButton.setOnClickListener(v -> registerUser());

        // Validación de la contraseña mientras el usuario escribe
        passwordEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String password = passwordEditText.getText().toString().trim();

                // Verificar mayúsculas
                if (!password.matches(".*[A-Z].*")) {
                    uppercaseTextView.setVisibility(TextView.VISIBLE);
                } else {
                    uppercaseTextView.setVisibility(TextView.GONE);
                }

                // Verificar números
                if (!password.matches(".*\\d.*")) {
                    numberTextView.setVisibility(TextView.VISIBLE);
                } else {
                    numberTextView.setVisibility(TextView.GONE);
                }

                // Verificar caracteres especiales
                if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                    specialCharTextView.setVisibility(TextView.VISIBLE);
                } else {
                    specialCharTextView.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        // Validaciones de campos
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingresa un correo válido");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Ingresa un nombre de usuario");
            return;
        }

        // Registrar usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar datos del usuario en Firestore
                            saveUserDataToFirestore(user.getUid(), username, user.getEmail());
                        }
                    } else {
                        // Obtener el error específico de Firebase
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error en el registro: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

    }


    private void saveUserDataToFirestore(String uid, String username, String email) {
        // Crear un objeto User con los datos del registro
        User user = new User(username, email);

        // Guardar los datos en Firestore en la colección "users"
        db.collection("users").document(uid)
                .set(user) // Guardamos el objeto 'user' en Firestore
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                    // Redirigir al mapa
                    goToMapActivity();
                })
                .addOnFailureListener(e -> {
                    // Mostrar un mensaje si falla
                    Toast.makeText(this, "Error al guardar los datos en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void goToMapActivity() {
        // Redirigir al mapa
        Intent intent = new Intent(registroApp.this, MapActivity.class);
        startActivity(intent);
        finish();
    }
}
