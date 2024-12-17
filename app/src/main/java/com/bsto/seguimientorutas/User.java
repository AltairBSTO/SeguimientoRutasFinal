package com.bsto.seguimientorutas;

public class User {
    private String username;
    private String email;

    public User() {
        // Constructor vacío necesario para Firestore
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters y setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
