package com.cde.appveterinaria.Modelos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cde.appveterinaria.R;
import java.util.List;
import java.util.Locale;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ViewHolder> {

    private Context context;
    private List<Servicio> listaServicios;
    private OnServicioClickListener listener;

    // Interfaz para manejar clics (editar o eliminar)
    public interface OnServicioClickListener {
        void onServicioClick(Servicio servicio);
        void onServicioLongClick(Servicio servicio); // Útil para eliminar
    }

    public ServicioAdapter(Context context, List<Servicio> listaServicios, OnServicioClickListener listener) {
        this.context = context;
        this.listaServicios = listaServicios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_servicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Servicio servicio = listaServicios.get(position);

        holder.tvNombre.setText(servicio.getNombreServicio());
        holder.tvCategoria.setText("Categoría: " + servicio.getCategoria());
        holder.tvDescripcion.setText(servicio.getDescripcion());
        holder.tvDuracion.setText("⏱ " + servicio.getDuracionMinutos() + " min");

        // Formatear el costo a dos decimales
        holder.tvCosto.setText(String.format(Locale.getDefault(), "$%.2f", servicio.getCosto()));

        // Cambiar color del estado si está disponible o no
        if (servicio.isDisponible()) {
            holder.tvEstado.setText("Disponible");
            holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvEstado.setText("No disponible");
            holder.tvEstado.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        // Eventos de clic
        holder.itemView.setOnClickListener(v -> listener.onServicioClick(servicio));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onServicioLongClick(servicio);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvDescripcion, tvDuracion, tvCosto, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreServicioItem);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaItem);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionItem);
            tvDuracion = itemView.findViewById(R.id.tvDuracionItem);
            tvCosto = itemView.findViewById(R.id.tvCostoItem);
            tvEstado = itemView.findViewById(R.id.tvEstadoServicio);
        }
    }
}