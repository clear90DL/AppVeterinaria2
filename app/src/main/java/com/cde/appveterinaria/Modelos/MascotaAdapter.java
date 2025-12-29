package com.cde.appveterinaria.Modelos;

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

public class MascotaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Mascota> listaMascotas;
    private OnMascotaListener listener;
    private boolean esAdmin;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Tipos de vista
    private static final int TYPE_CLIENTE = 1;
    private static final int TYPE_ADMIN = 2;

    // Interface para manejar eventos
    public interface OnMascotaListener {
        void onEditarMascota(Mascota mascota, int position);
        void onEliminarMascota(Mascota mascota, int position);
        void onMascotaClick(Mascota mascota, int position);
    }

    // Constructor actualizado
    public MascotaAdapter(Context context, List<Mascota> listaMascotas, boolean esAdmin, OnMascotaListener listener) {
        this.context = context;
        this.listaMascotas = listaMascotas;
        this.esAdmin = esAdmin;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return esAdmin ? TYPE_ADMIN : TYPE_CLIENTE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADMIN) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_mascota_admin, parent, false);
            return new ViewHolderAdmin(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_mascota, parent, false);
            return new ViewHolderCliente(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mascota mascota = listaMascotas.get(position);

        if (mascota == null) return;

        if (getItemViewType(position) == TYPE_ADMIN) {
            ViewHolderAdmin vh = (ViewHolderAdmin) holder;

            // Configurar datos básicos
            vh.tvNombre.setText(mascota.getNombre());
            vh.tvEspecie.setText("Especie: " + mascota.getEspecie());
            vh.tvRaza.setText("Raza: " + (mascota.getRaza() != null ? mascota.getRaza() : "No especificada"));
            vh.tvEdad.setText("Edad: " + mascota.getEdad() + " años");
            vh.tvSexo.setText("Sexo: " + mascota.getSexo());

            // Mostrar dueño
            String nombreDueño = mascota.getNombreDueño();
            if (nombreDueño != null && !nombreDueño.isEmpty()) {
                vh.tvDueño.setText("Dueño: " + nombreDueño);
            } else {
                // Si no tiene nombre del dueño, buscar por ID
                String uid = mascota.getIdUsuario();
                if (uid != null) {
                    buscarNombreDueño(uid, vh.tvDueño);
                } else {
                    vh.tvDueño.setText("Dueño: No especificado");
                }
            }

            // Botón Editar
            vh.btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarMascota(mascota, position);
                }
            });

            // Botón Eliminar
            vh.btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarMascota(mascota, position);
                }
            });

            // Click en toda la tarjeta
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMascotaClick(mascota, position);
                }
            });

        } else {
            // Vista de cliente
            ViewHolderCliente vh = (ViewHolderCliente) holder;

            vh.tvNombre.setText(mascota.getNombre());
            vh.tvEspecie.setText("Especie: " + mascota.getEspecie());
            vh.tvRaza.setText("Raza: " + (mascota.getRaza() != null ? mascota.getRaza() : "No especificada"));
            vh.tvEdad.setText("Edad: " + mascota.getEdad() + " años");
            vh.tvSexo.setText("Sexo: " + mascota.getSexo());

            // Mostrar botones solo si el cliente es el dueño
            if (mascota.getIdUsuario() != null && mascota.getIdUsuario().equals(getCurrentUserId())) {
                vh.btnEditar.setVisibility(View.VISIBLE);
                vh.btnEliminar.setVisibility(View.VISIBLE);

                vh.btnEditar.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditarMascota(mascota, position);
                    }
                });

                vh.btnEliminar.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEliminarMascota(mascota, position);
                    }
                });
            } else {
                vh.btnEditar.setVisibility(View.GONE);
                vh.btnEliminar.setVisibility(View.GONE);
            }

            // Click en toda la tarjeta
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMascotaClick(mascota, position);
                }
            });
        }
    }

    // Método para buscar nombre del dueño
    private void buscarNombreDueño(String uid, TextView textView) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            textView.setText("Dueño: " + nombre);
                        }
                    }
                });
    }

    // Método para obtener el ID del usuario actual
    private String getCurrentUserId() {
        // Necesitarás pasar el FirebaseAuth o el user ID desde la actividad
        // Por ahora retornamos null y manejaremos en la actividad
        return null;
    }

    @Override
    public int getItemCount() {
        return listaMascotas != null ? listaMascotas.size() : 0;
    }

    // Actualizar lista
    public void actualizarLista(List<Mascota> nuevaLista) {
        listaMascotas = nuevaLista;
        notifyDataSetChanged();
    }

    // Eliminar item de la lista
    public void eliminarItem(int position) {
        if (position >= 0 && position < listaMascotas.size()) {
            listaMascotas.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder para el Administrador
    public static class ViewHolderAdmin extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEspecie, tvRaza, tvEdad, tvSexo, tvDueño;
        Button btnEditar, btnEliminar;

        public ViewHolderAdmin(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMascotaItem);
            tvEspecie = itemView.findViewById(R.id.tvEspecieMascotaItem);
            tvRaza = itemView.findViewById(R.id.tvRazaMascotaItem);
            tvEdad = itemView.findViewById(R.id.tvEdadMascotaItem);
            tvSexo = itemView.findViewById(R.id.tvSexoMascotaItem);
            tvDueño = itemView.findViewById(R.id.txtDueñoMascota);
            btnEditar = itemView.findViewById(R.id.btnEditarMascota);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMascota);
        }
    }

    // ViewHolder para el Cliente
    public static class ViewHolderCliente extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEspecie, tvRaza, tvEdad, tvSexo;
        Button btnEditar, btnEliminar;

        public ViewHolderCliente(@NonNull View itemView) {
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