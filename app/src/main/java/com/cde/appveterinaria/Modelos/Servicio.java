package com.cde.appveterinaria.Modelos;

import java.util.HashMap;
import java.util.Map;

public class Servicio {
    private String idServicio;
    private String nombreServicio;
    private String descripcion;
    private double costo;
    private String categoria;
    private int duracionMinutos;
    private boolean disponible;

    // Constructor
    public Servicio() {}

    public Servicio(String nombre, String desc, double costo, String categoria, int duracion) {
        this.nombreServicio = nombre;
        this.descripcion = desc;
        this.costo = costo;
        this.categoria = categoria;
        this.duracionMinutos = duracion;
        this.disponible = true;
    }

    // Getters y Setters (Asegúrate de tener TODOS)
    public String getIdServicio() { return idServicio; }
    public void setIdServicio(String id) { this.idServicio = id; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombre) { this.nombreServicio = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String desc) { this.descripcion = desc; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracion) { this.duracionMinutos = duracion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    // Método toMap() para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nombreServicio", nombreServicio);
        map.put("descripcion", descripcion);
        map.put("costo", costo);
        map.put("categoria", categoria);
        map.put("duracionMinutos", duracionMinutos);
        map.put("disponible", disponible);
        return map;
    }
}