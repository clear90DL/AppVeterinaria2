package com.cde.appveterinaria.Modelos;

public class Usuario {
    private String uid;
    private String nombre;
    private String email;
    private String rol;
    private String fechaRegistroFormateada;
    private boolean activo;
    private boolean emailVerificado;

    public Usuario() {} // Requerido por Firebase

    // Getters y Setters...
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public String getFechaRegistroFormateada() { return fechaRegistroFormateada; }
    public boolean isActivo() { return activo; }
}