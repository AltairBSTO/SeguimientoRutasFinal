package com.bsto.seguimientorutas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.location.Geocoder;
import android.location.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnIniciarRuta, btnDetenerRuta, btnCerrarSesion, btnVerHistorial;
    private boolean isTracking = false;
    private List<LatLng> routePoints = new ArrayList<>();
    private String routeStartTime;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializar botones
        btnIniciarRuta = findViewById(R.id.btnIniciarRuta);
        btnDetenerRuta = findViewById(R.id.btnDetenerRuta);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnVerHistorial = findViewById(R.id.btnVerHistorial); // Botón para ver historial

        btnIniciarRuta.setOnClickListener(v -> iniciarRuta());
        btnDetenerRuta.setOnClickListener(v -> detenerRutaYGuardar());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Configurar el botón para ver el historial
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, HistorialRutasActivity.class);
            startActivity(intent); // Ir a la actividad del historial
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            habilitarUbicacion();
        }
    }

    private void habilitarUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(ubicacionActual).title("Tu ubicación actual"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f));
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener la ubicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                habilitarUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación necesario para usar el mapa", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void iniciarRuta() {
        if (isTracking) {
            Toast.makeText(this, "Ya estás grabando una ruta", Toast.LENGTH_SHORT).show();
            return;
        }

        isTracking = true;
        routePoints.clear();
        routeStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        startLocationUpdates();
        Toast.makeText(this, "Ruta iniciada", Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    for (android.location.Location location : locationResult.getLocations()) {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            routePoints.add(currentLatLng);

                            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Ubicación Actual"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void detenerRutaYGuardar() {
        if (!isTracking) {
            Toast.makeText(this, "No hay ninguna ruta activa", Toast.LENGTH_SHORT).show();
            return;
        }

        isTracking = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);

        PolylineOptions polylineOptions = new PolylineOptions().addAll(routePoints).clickable(false);
        mMap.addPolyline(polylineOptions);

        // Mostrar un cuadro de diálogo para ingresar el nombre de la ruta
        mostrarDialogoParaNombreRuta();
    }

    private void mostrarDialogoParaNombreRuta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre de la Ruta");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nombreRuta = input.getText().toString();
            if (!nombreRuta.isEmpty()) {
                guardarRutaEnFirestore(nombreRuta);
            } else {
                // Si no se ingresa un nombre, usar el nombre por defecto
                guardarRutaEnFirestore("Ruta " + routeStartTime);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void guardarRutaEnFirestore(String nombreRuta) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        List<GeoPoint> geoPoints = new ArrayList<>();
        for (LatLng point : routePoints) {
            geoPoints.add(new GeoPoint(point.latitude, point.longitude));
        }

        Route route = new Route(
                nombreRuta, // Nombre personalizado de la ruta
                user.getUid(),
                routeStartTime,
                geoPoints
        );

        db.collection("rutas")
                .add(route)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Ruta guardada en Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la ruta: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
