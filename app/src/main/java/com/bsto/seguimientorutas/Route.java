package com.bsto.seguimientorutas;

import com.google.firebase.firestore.GeoPoint;
import java.util.List;

public class Route {
    private String nombreRuta;
    private String usuario;
    private String fechaInicio;
    private List<GeoPoint> coordenadas;

    // Constructor vacío necesario para Firestore
    public Route() {
    }

    // Constructor con parámetros
    public Route(String nombreRuta, String usuario, String fechaInicio, List<GeoPoint> coordenadas) {
        this.nombreRuta = nombreRuta;
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.coordenadas = coordenadas;
    }

    // Getters y setters
    public String getNombreRuta() {
        return nombreRuta;
    }

    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public List<GeoPoint> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<GeoPoint> coordenadas) {
        this.coordenadas = coordenadas;
    }
}
