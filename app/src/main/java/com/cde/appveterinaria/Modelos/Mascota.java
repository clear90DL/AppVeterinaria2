package com.cde.appveterinaria.Modelos;

import java.util.HashMap;
import java.util.Map;

public class Mascota {
    private String idMascota;
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private String idUsuario; // Referencia al UID del usuario en Firebase
    private String sexo;
    private String fotoUrl;
    private long fechaRegistro;

    // Constructor vacío necesario para Firestore
    public Mascota() {}

    // Constructor completo
    public Mascota(String nombre, String especie, String raza, int edad,
                   String idUsuario, String sexo) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edad = edad;
        this.idUsuario = idUsuario;
        this.sexo = sexo;
        this.fechaRegistro = System.currentTimeMillis();
    }

    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("especie", especie);
        map.put("raza", raza);
        map.put("edad", edad);
        map.put("idUsuario", idUsuario);
        map.put("sexo", sexo);
        map.put("fotoUrl", fotoUrl != null ? fotoUrl : "");
        map.put("fechaRegistro", fechaRegistro);
        map.put("fechaRegistroFormateada",
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                        java.util.Locale.getDefault()).format(new java.util.Date(fechaRegistro)));
        return map;
    }

    // Getters y Setters
    public String getIdMascota() { return idMascota; }
    public void setIdMascota(String idMascota) { this.idMascota = idMascota; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public long getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}