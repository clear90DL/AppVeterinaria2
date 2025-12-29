package com.cde.appveterinaria.Modelos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {
    private List<Usuario> listaUsuarios;

    public UsuarioAdapter(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Puedes reusar un layout de item simple o crear uno nuevo
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new UsuarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario u = listaUsuarios.get(position);
        holder.tvNombre.setText(u.getNombre() + " (" + u.getRol() + ")");
        holder.tvEmail.setText(u.getEmail() + " - Reg: " + u.getFechaRegistroFormateada());

        // Color basado en si está activo
        holder.tvNombre.setTextColor(u.isActivo() ? Color.BLACK : Color.RED);
    }

    @Override
    public int getItemCount() { return listaUsuarios.size(); }

    class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail;
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(android.R.id.text1);
            tvEmail = itemView.findViewById(android.R.id.text2);
        }
    }
}