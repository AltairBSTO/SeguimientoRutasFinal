package com.bsto.seguimientorutas;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistorialRutasActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListView listViewHistorial;
    private Button btnVolver;
    private TextView textViewDetallesRuta; // Para mostrar los detalles de la ruta seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listViewHistorial = findViewById(R.id.listViewHistorial);
        btnVolver = findViewById(R.id.btnVolver);
        textViewDetallesRuta = findViewById(R.id.textViewDetallesRuta); // Obtener el TextView

        // Configurar el botón para volver a la actividad del mapa
        btnVolver.setOnClickListener(v -> finish());

        // Cargar el historial de rutas del usuario actual
        cargarHistorialUsuario();
    }

    private void cargarHistorialUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        db.collection("rutas")
                .whereEqualTo("usuario", userId) // Filtrar por el UID del usuario actual
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> rutas = new ArrayList<>();
                    List<String> rutaIds = new ArrayList<>(); // Lista para almacenar los IDs de las rutas
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreRuta = document.getString("nombreRuta");
                        if (nombreRuta != null) {
                            rutas.add(nombreRuta);
                            rutaIds.add(document.getId()); // Almacenar el ID de la ruta
                        }
                    }

                    if (rutas.isEmpty()) {
                        Toast.makeText(this, "No tienes rutas guardadas", Toast.LENGTH_SHORT).show();
                    }

                    // Mostrar los datos en el ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rutas);
                    listViewHistorial.setAdapter(adapter);

                    // Configurar el listener para manejar los clics en las rutas
                    listViewHistorial.setOnItemClickListener((parent, view, position, id) -> {
                        String rutaId = rutaIds.get(position); // Obtener el ID de la ruta seleccionada
                        mostrarDetallesRuta(rutaId);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarDetallesRuta(String rutaId) {
        db.collection("rutas")
                .document(rutaId) // Obtener la ruta específica por su ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreRuta = documentSnapshot.getString("nombreRuta");
                        List<com.google.firebase.firestore.GeoPoint> puntosRuta = (List<com.google.firebase.firestore.GeoPoint>) documentSnapshot.get("coordenadas"); // Lista de coordenadas

                        if (puntosRuta != null && !puntosRuta.isEmpty()) {
                            StringBuilder detalles = new StringBuilder();
                            detalles.append("Nombre Ruta: ").append(nombreRuta).append("\n");

                            // Mostrar los puntos geográficos
                            for (int i = 0; i < puntosRuta.size(); i++) {
                                com.google.firebase.firestore.GeoPoint punto = puntosRuta.get(i);
                                if (punto != null) {
                                    detalles.append(i).append("\n[")
                                            .append(punto.getLatitude()).append("° S, ")
                                            .append(punto.getLongitude()).append("° W]\n(punto geográfico)\n");
                                }
                            }

                            // Mostrar los detalles en el TextView
                            textViewDetallesRuta.setVisibility(View.VISIBLE); // Hacer visible el TextView
                            textViewDetallesRuta.setText(detalles.toString());
                        } else {
                            Toast.makeText(HistorialRutasActivity.this, "No se encontraron puntos geográficos en esta ruta.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HistorialRutasActivity.this, "Ruta no encontrada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HistorialRutasActivity.this, "Error al cargar los detalles de la ruta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
