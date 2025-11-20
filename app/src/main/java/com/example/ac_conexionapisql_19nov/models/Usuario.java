package com.example.ac_conexionapisql_19nov.models;

public class Usuario {
    private Integer idUsuario;
    private String nombre;
    private String email;
    private String password;
    private int idRol;
    private Rol rol;

    // Constructor vacío
    public Usuario() {}

    // Constructor con todos los campos
    public Usuario(Integer idUsuario, String nombre, String email, String password, int idRol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
    }

    // Constructor sin la llave primaria
    public Usuario(String nombre, String email, String password, int idRol) {
        this.idUsuario = null;  // null para que la API lo genere
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
        if (rol != null) {
            this.idRol = rol.getIdRol();  // Mantener idRol sincronizado
        }
    }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}

