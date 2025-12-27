package com.cde.appveterinaria.Modelos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cde.appveterinaria.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private Context context;
    private List<Mascota> listaMascotas;
    private FirebaseFirestore db;
    private OnMascotaListener listener;

    // Interface para manejar clicks
    public interface OnMascotaListener {
        void onEditarMascota(Mascota mascota, int position);
    }

    public MascotaAdapter(Context context, List<Mascota> listaMascotas, OnMascotaListener listener) {
        this.context = context;
        this.listaMascotas = listaMascotas;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Mascota mascota = listaMascotas.get(position);

        holder.tvNombre.setText(mascota.getNombre());
        holder.tvEspecie.setText(mascota.getEspecie());
        holder.tvRaza.setText(mascota.getRaza() != null && !mascota.getRaza().isEmpty()
                ? mascota.getRaza() : "No especificada");
        holder.tvEdad.setText(mascota.getEdad() + " años");
        holder.tvSexo.setText(mascota.getSexo());

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Mascota")
                    .setMessage("¿Estás seguro de eliminar a " + mascota.getNombre() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        eliminarMascota(mascota, position);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botón Editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarMascota(mascota, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMascotas.size();
    }

    private void eliminarMascota(Mascota mascota, int position) {
        db.collection("mascotas")
                .document(mascota.getIdMascota())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaMascotas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaMascotas.size());
                    Toast.makeText(context, "✅ Mascota eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "❌ Error al eliminar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    public static class MascotaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEspecie, tvRaza, tvEdad, tvSexo;
        Button btnEditar, btnEliminar;

        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMascotaItem);
            tvEspecie = itemView.findViewById(R.id.tvEspecieMascotaItem);
            tvRaza = itemView.findViewById(R.id.tvRazaMascotaItem);
            tvEdad = itemView.findViewById(R.id.tvEdadMascotaItem);
            tvSexo = itemView.findViewById(R.id.tvSexoMascotaItem);
            btnEditar = itemView.findViewById(R.id.btnEditarMascota);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMascota);
        }
    }
}