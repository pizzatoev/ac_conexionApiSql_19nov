package com.example.ac_conexionapisql_19nov.models;

public class Rol {
    private int idRol;
    private String nombreRol;

    // Constructor vacío
    public Rol() {}

    // Constructor con todos los campos
    public Rol(int idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    // Constructor sin la llave primaria
    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    @Override
    public String toString() {
        return nombreRol;
    }
}

