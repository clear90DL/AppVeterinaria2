package com.cde.appveterinaria.Modelos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cde.appveterinaria.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.ViewHolder> {
    private Context context;
    private List<Cita> listaCitas;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CitaAdapter(Context context, List<Cita> listaCitas) {
        this.context = context;
        this.listaCitas = listaCitas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cita cita = listaCitas.get(position);

        holder.tvMascota.setText("Mascota: " + cita.getNombreMascota());
        holder.tvServicio.setText("Servicio: " + cita.getServicio());

        // Manejo de la fecha tipo Date
        String fechaFormateada = cita.getFechaCita() != null ? dateFormat.format(cita.getFechaCita()) : "Sin fecha";
        holder.tvFechaHora.setText("Fecha: " + fechaFormateada + " - Hora: " + cita.getHoraCita());

        holder.tvEstado.setText("Estado: " + cita.getEstado());
        holder.tvVeterinario.setText("Veterinario: " + cita.getVeterinario());
    }

    @Override
    public int getItemCount() {
        return listaCitas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMascota, tvServicio, tvFechaHora, tvEstado, tvVeterinario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMascota = itemView.findViewById(R.id.tvMascotaItem);
            tvServicio = itemView.findViewById(R.id.tvServicioItem);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHoraItem);
            tvEstado = itemView.findViewById(R.id.tvEstadoItem);
            tvVeterinario = itemView.findViewById(R.id.tvVeterinarioItem);
        }
    }
}