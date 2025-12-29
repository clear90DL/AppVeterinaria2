package com.cde.appveterinaria.Modelos;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cde.appveterinaria.FragmentoAdministrador.InicioAdministrador;
import com.cde.appveterinaria.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.ViewHolder> {
    private Context context;
    private List<Cita> listaCitas;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CitaAdapter(Context context, List<Cita> listaCitas) {
        this.context = context;
        this.listaCitas = listaCitas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que este layout existe
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cita cita = listaCitas.get(position);

        if (cita == null) return;

        try {
            // 1. Mostrar datos básicos
            holder.tvMascota.setText(cita.getNombreMascota() != null ?
                    cita.getNombreMascota() : "Sin nombre");

            holder.tvServicio.setText(cita.getServicio() != null ?
                    "Servicio: " + cita.getServicio() : "Sin servicio");

            // 2. Fecha y hora
            String fechaStr = "Sin fecha";
            if (cita.getFechaCita() != null) {
                fechaStr = dateFormat.format(cita.getFechaCita());
            }

            String horaStr = cita.getHoraCita() != null ? cita.getHoraCita() : "Sin hora";
            holder.tvFechaHora.setText("📅 " + fechaStr + " - " + horaStr);

            // 3. Estado
            String estado = cita.getEstado() != null ? cita.getEstado().toLowerCase() : "pendiente";
            holder.tvEstado.setText(estado.toUpperCase());
            configurarColorEstado(holder.tvEstado, estado);

            // 4. Veterinario (si existe ese campo)
            if (holder.tvVeterinario != null) {
                holder.tvVeterinario.setText(cita.getVeterinario() != null ?
                        "Veterinario: " + cita.getVeterinario() : "");
            }

            // 5. Distinción entre Admin y Cliente
            boolean esAdmin = context instanceof InicioAdministrador;

            if (holder.tvDueño != null) {
                if (esAdmin) {
                    holder.tvDueño.setVisibility(View.VISIBLE);
                    holder.tvDueño.setText(cita.getNombreUsuario() != null ?
                            "Dueño: " + cita.getNombreUsuario() : "Dueño: Desconocido");
                } else {
                    holder.tvDueño.setVisibility(View.GONE);
                }
            }

            if (holder.btnAceptar != null && holder.btnCancelar != null) {
                if (esAdmin) {
                    holder.btnAceptar.setVisibility(View.VISIBLE);
                    holder.btnCancelar.setVisibility(View.VISIBLE);

                    holder.btnAceptar.setOnClickListener(v ->
                            actualizarEstado(cita.getIdCita(), "aceptada"));
                    holder.btnCancelar.setOnClickListener(v ->
                            actualizarEstado(cita.getIdCita(), "cancelada"));
                } else {
                    holder.btnAceptar.setVisibility(View.GONE);
                    holder.btnCancelar.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al mostrar cita", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarColorEstado(TextView tv, String estado) {
        switch (estado) {
            case "pendiente":
                tv.setTextColor(Color.parseColor("#FF9800")); // Naranja
                break;
            case "aceptada":
            case "confirmada":
                tv.setTextColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "cancelada":
            case "rechazada":
                tv.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
            case "completada":
                tv.setTextColor(Color.parseColor("#2196F3")); // Azul
                break;
            default:
                tv.setTextColor(Color.GRAY);
                break;
        }
    }

    private void actualizarEstado(String idCita, String nuevoEstado) {
        if (idCita == null || idCita.isEmpty()) return;

        db.collection("citas").document(idCita)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cita " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged(); // Refrescar la lista
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al actualizar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return listaCitas != null ? listaCitas.size() : 0;
    }

    // Actualizar la lista
    public void actualizarLista(List<Cita> nuevaLista) {
        listaCitas = nuevaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMascota, tvDueño, tvServicio, tvFechaHora, tvEstado, tvVeterinario;
        Button btnAceptar, btnCancelar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Estos IDs DEBEN coincidir con tu item_cita_admin.xml
            tvMascota = itemView.findViewById(R.id.tvNombreMascotaCita);
            tvDueño = itemView.findViewById(R.id.tvDueñoCita);
            tvServicio = itemView.findViewById(R.id.tvServicioItem);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvEstado = itemView.findViewById(R.id.tvEstadoCita);
            tvVeterinario = itemView.findViewById(R.id.tvVeterinarioItem);

            // Botones de administración
            btnAceptar = itemView.findViewById(R.id.btnAceptarCita);
            btnCancelar = itemView.findViewById(R.id.btnCancelarCita);
        }
    }
}