package com.cde.appveterinaria.Modelos;

import com.google.firebase.firestore.DocumentSnapshot; // ← AGREGAR ESTA LÍNEA

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Cita {
    private String idCita;
    private String idUsuario;
    private String nombreUsuario;
    private String idMascota;
    private String nombreMascota;
    private String servicio;
    private Date fechaCita;
    private String horaCita;
    private String veterinario;
    private String notas;
    private String estado;

    // ✅ CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE)
    public Cita() {
        this.estado = "pendiente"; // valor por defecto
    }

    // Constructor con parámetros
    public Cita(String idUsuario, String nombreUsuario, String idMascota,
                String nombreMascota, String servicio, Date fechaCita,
                String horaCita, String veterinario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.servicio = servicio;
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.veterinario = veterinario;
        this.estado = "pendiente";
    }

    // ✅ GETTERS Y SETTERS (TODOS)
    public String getIdCita() { return idCita; }
    public void setIdCita(String idCita) { this.idCita = idCita; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getIdMascota() { return idMascota; }
    public void setIdMascota(String idMascota) { this.idMascota = idMascota; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public Date getFechaCita() { return fechaCita; }
    public void setFechaCita(Date fechaCita) { this.fechaCita = fechaCita; }

    public String getHoraCita() { return horaCita; }
    public void setHoraCita(String horaCita) { this.horaCita = horaCita; }

    public String getVeterinario() { return veterinario; }
    public void setVeterinario(String veterinario) { this.veterinario = veterinario; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // ✅ Si tienes fromFirestore, asegúrate de que maneje nulls
    public static Cita fromFirestore(DocumentSnapshot doc) {
        Cita cita = new Cita();
        cita.setIdCita(doc.getId());
        cita.setIdUsuario(doc.getString("idUsuario"));
        cita.setNombreUsuario(doc.getString("nombreUsuario"));
        cita.setIdMascota(doc.getString("idMascota"));
        cita.setNombreMascota(doc.getString("nombreMascota"));
        cita.setServicio(doc.getString("servicio"));
        cita.setFechaCita(doc.getDate("fechaCita"));
        cita.setHoraCita(doc.getString("horaCita"));
        cita.setVeterinario(doc.getString("veterinario"));
        cita.setNotas(doc.getString("notas"));

        String estado = doc.getString("estado");
        cita.setEstado(estado != null ? estado : "pendiente");

        return cita;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("idCita", idCita);
        map.put("idUsuario", idUsuario);
        map.put("nombreUsuario", nombreUsuario);
        map.put("idMascota", idMascota);
        map.put("nombreMascota", nombreMascota);
        map.put("servicio", servicio);
        map.put("fechaCita", fechaCita);
        map.put("horaCita", horaCita);
        map.put("veterinario", veterinario);
        map.put("notas", notas);
        map.put("estado", estado != null ? estado : "pendiente");
        return map;
    }
}