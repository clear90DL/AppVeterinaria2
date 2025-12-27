package com.cde.appveterinaria.FragmentoAdministrador;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cde.appveterinaria.Modelos.Servicio;
import com.cde.appveterinaria.Modelos.ServicioAdapter;
import com.cde.appveterinaria.Principal.LoginActivity;
import com.cde.appveterinaria.Principal.MainActivity;
import com.cde.appveterinaria.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InicioAdministrador extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private MaterialToolbar toolbar;
    private FrameLayout contenedorVistas;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Agregar Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel_administrador);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializar Firestore

        // Verificar que el usuario sea admin
        verificarRolUsuario();

        initViews();
        setupToolbar();
        setupDrawerLayout();
        setupNavigationView();
    }

    private void verificarRolUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            String email = currentUser.getEmail();
            if (email != null && email.equals("admin@gmail.com")) {
                Toast.makeText(this, "Bienvenido Administrador Especial", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bienvenido Administrador: " + email, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar1);
        contenedorVistas = findViewById(R.id.contenedorVistas);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Panel Administrador");
        }
    }

    private void setupDrawerLayout() {
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio_administrador) {
                mostrarVistaInicio();
            } else if (id == R.id.nav_perfil) {
                mostrarVistaPerfil();
            } else if (id == R.id.nav_usuarios_administrador) {
                mostrarVistaUsuarios();
            } else if (id == R.id.nav_cita_administrador) {
                mostrarVistaCitas();
            } else if (id == R.id.servicios_administrador) {
                mostrarVistaServicios();
            } else if (id == R.id.informes_administrador) {
                mostrarVistaInformes();
            } else if (id == R.id.nav_mascota_administrador) {
                mostrarVistaMascotas();
            } else if (id == R.id.cambiar_contraseña) {
                mostrarDialogoCambiarContraseña();
            } else if (id == R.id.btnCerrarSesion) {
                mostrarDialogoConfirmarCerrarSesion();
            }
            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Métodos para mostrar vistas
    private void mostrarVistaInicio() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.inicio_administrador, contenedorVistas);
        toolbar.setTitle("Panel Administrador");
    }

    private void mostrarVistaPerfil() {
        contenedorVistas.removeAllViews();
        View perfilView = getLayoutInflater().inflate(R.layout.dialog_perfil, contenedorVistas);
        toolbar.setTitle("Mi Perfil");

        // Cargar datos del perfil
        cargarDatosPerfil(perfilView);
    }

    private void cargarDatosPerfil(View view) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            TextView tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil);
            TextView tvEmailPerfil = view.findViewById(R.id.tvEmailPerfil);
            TextView tvEmailVerificado = view.findViewById(R.id.tvEmailVerificado);
            Button btnReenviarVerificacion = view.findViewById(R.id.btnReenviarVerificacionPerfil);
            Button btnCerrarPerfil = view.findViewById(R.id.btnCerrarPerfil);

            // Datos básicos de Firebase Auth
            String email = user.getEmail();
            String nombre = user.getDisplayName();

            tvEmailPerfil.setText("Email: " + email);
            tvNombrePerfil.setText("Nombre: " + (nombre != null ? nombre : "No especificado"));

            if (user.isEmailVerified()) {
                tvEmailVerificado.setText("Email verificado: Sí");
                btnReenviarVerificacion.setVisibility(View.GONE);
            } else {
                tvEmailVerificado.setText("Email verificado: No");
                btnReenviarVerificacion.setVisibility(View.VISIBLE);
            }

            // Buscar datos adicionales en Firestore
            buscarDatosAdicionalesEnFirestore(user.getUid(), tvNombrePerfil);

            // Botón para reenviar verificación
            btnReenviarVerificacion.setOnClickListener(v -> {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Email de verificación enviado", Toast.LENGTH_LONG).show();
                            }
                        });
            });

            // Botón para cerrar
            btnCerrarPerfil.setOnClickListener(v -> {
                mostrarVistaInicio(); // Volver al inicio
            });
        }
    }

    private void buscarDatosAdicionalesEnFirestore(String uid, View parentView) {
        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Nombre
                            String nombreFirestore = document.getString("nombre");
                            if (nombreFirestore != null && !nombreFirestore.isEmpty()) {
                                TextView tvNombrePerfil = parentView.findViewById(R.id.tvNombrePerfil);
                                if (tvNombrePerfil != null) {
                                    tvNombrePerfil.setText("Nombre: " + nombreFirestore);
                                }
                            }

                            // Rol
                            String rol = document.getString("rol");
                            TextView tvRol = parentView.findViewById(R.id.tvRolPerfil);
                            if (tvRol != null) {
                                if (rol != null) {
                                    tvRol.setText("Rol: " + rol);
                                    tvRol.setVisibility(View.VISIBLE);
                                } else {
                                    tvRol.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
    }

    private void mostrarVistaUsuarios() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.gestion_usuarios_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Usuarios");
    }

    private void mostrarVistaMascotas() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.gestion_mascota_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Mascotas");
    }

    private void mostrarVistaCitas() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.gestion_citas_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Citas");
    }

    private void mostrarVistaServicios() {
        contenedorVistas.removeAllViews();
        // Inflamos el XML que acabamos de crear
        View view = getLayoutInflater().inflate(R.layout.gestion_servicios_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Servicios");

        // 1. Vincular el botón de agregar
        Button btnAgregar = view.findViewById(R.id.btnAgregarNuevoServicio);
        btnAgregar.setOnClickListener(v -> mostrarDialogoRegistroServicio());

        // 2. Configurar el RecyclerView (el método que ya tienes escrito)
        configurarListaServicios(view);
    }
    private void mostrarVistaInformes() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.gestion_informes_administrador, contenedorVistas);
        toolbar.setTitle("Generación de Informes");
    }
    private void mostrarDialogoCambiarContraseña() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.dialogo_cambiar_password, contenedorVistas);
        toolbar.setTitle("Cambio de contraseña");
    }


    private boolean validarCamposCambioContraseña(String actual, String nueva, String confirmar) {
        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (nueva.length() < 6) {
            Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!nueva.equals(confirmar)) {
            Toast.makeText(this, "Las nuevas contraseñas no coinciden",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (actual.equals(nueva)) {
            Toast.makeText(this, "La nueva contraseña debe ser diferente a la actual",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void cambiarContraseña(String contraseñaActual, String nuevaContraseña, Dialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar credenciales actuales
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), contraseñaActual);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Actualizar contraseña
                        user.updatePassword(nuevaContraseña)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(InicioAdministrador.this,
                                                "✅ Contraseña cambiada exitosamente",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        String errorMessage = "Error al cambiar contraseña";
                                        if (updateTask.getException() != null) {
                                            errorMessage = updateTask.getException().getMessage();
                                        }
                                        Toast.makeText(InicioAdministrador.this, errorMessage,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(InicioAdministrador.this,
                                "❌ Contraseña actual incorrecta",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Diálogo para confirmar cerrar sesión
    private void mostrarDialogoConfirmarCerrarSesion() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_cerrar_sesion);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        dialog.findViewById(R.id.btnConfirmarCerrarSesion).setOnClickListener(v -> {
            cerrarSesionFirebase();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnCancelarCerrarSesion).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Diálogo para confirmar salir de la app
    private void mostrarDialogoSalirApp() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogo_salir_app);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        dialog.findViewById(R.id.btnSalirApp).setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });

        dialog.findViewById(R.id.btnCancelarSalir).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Método para cerrar sesión en Firebase
    private void cerrarSesionFirebase() {
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarDialogoRegistroServicio() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.registrar_servicio_admin);

        // 1. Configurar el Spinner correctamente
        Spinner spinnerCategoria = dialog.findViewById(R.id.spinnerCategoriaServicio);
        String[] categorias = {"Consultas", "Vacunación", "Cirugía", "Estética", "Laboratorio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // 2. Referencias (Quitamos el EditText Cat que daba error)
        EditText etNombre = dialog.findViewById(R.id.etNombreServicio);
        EditText etDesc = dialog.findViewById(R.id.etDescripcionServicio);
        EditText etCosto = dialog.findViewById(R.id.etCostoServicio);
        EditText etDuracion = dialog.findViewById(R.id.etDuracionServicio);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarServicio);

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String costoStr = etCosto.getText().toString().trim();
            String duracionStr = etDuracion.getText().toString().trim();
            // OBTENEMOS LA CATEGORÍA SELECCIONADA
            String categoriaSeleccionada = spinnerCategoria.getSelectedItem().toString();

            if (nombre.isEmpty() || costoStr.isEmpty()) {
                Toast.makeText(this, "Nombre y Costo son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double costo = Double.parseDouble(costoStr);
                int duracion = duracionStr.isEmpty() ? 0 : Integer.parseInt(duracionStr);

                // USAMOS LA CATEGORÍA REAL
                Servicio nuevo = new Servicio(nombre, desc, costo, categoriaSeleccionada, duracion);

                db.collection("servicios")
                        .add(nuevo.toMap())
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "✅ Servicio agregado", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            mostrarVistaServicios();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingresa números válidos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void configurarListaServicios(View view) {
        RecyclerView rvServicios = view.findViewById(R.id.rvServiciosAdmin);
        rvServicios.setLayoutManager(new LinearLayoutManager(this));

        List<Servicio> listaServicios = new ArrayList<>();

        // Inicializar el adaptador con el listener
        ServicioAdapter adapter = new ServicioAdapter(this, listaServicios, new ServicioAdapter.OnServicioClickListener() {
            @Override
            public void onServicioClick(Servicio servicio) {
                // Acción al tocar: Abrir diálogo para editar
                mostrarDialogoEditarServicio(servicio);
            }

            @Override
            public void onServicioLongClick(Servicio servicio) {
                // Acción al mantener presionado: Confirmar eliminación
                confirmarEliminarServicio(servicio);
            }
        });

        rvServicios.setAdapter(adapter);

        // Cargar desde Firestore
        db.collection("servicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaServicios.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Servicio s = doc.toObject(Servicio.class);
                        if (s != null) {
                            s.setIdServicio(doc.getId());
                            listaServicios.add(s);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void confirmarEliminarServicio(Servicio servicio) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Servicio")
                .setMessage("¿Estás seguro de eliminar " + servicio.getNombreServicio() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("servicios").document(servicio.getIdServicio())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                mostrarVistaServicios(); // Refrescar vista
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void mostrarDialogoEditarServicio(Servicio servicio) {
        Dialog dialog = new Dialog(this);
        // CORRECCIÓN: Usar el layout del formulario, no el de la lista
        dialog.setContentView(R.layout.registrar_servicio_admin);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloServicio);
        EditText etNombre = dialog.findViewById(R.id.etNombreServicio);
        EditText etDesc = dialog.findViewById(R.id.etDescripcionServicio);
        EditText etCosto = dialog.findViewById(R.id.etCostoServicio);
        EditText etDuracion = dialog.findViewById(R.id.etDuracionServicio);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarServicio);
        Spinner spinnerCategoria = dialog.findViewById(R.id.spinnerCategoriaServicio);

        // Configurar Spinner en edición también
        String[] categorias = {"Consultas", "Vacunación", "Cirugía", "Estética", "Laboratorio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        spinnerCategoria.setAdapter(adapter);

        // Llenar datos actuales
        if (tvTitulo != null) tvTitulo.setText("Editar Servicio");
        etNombre.setText(servicio.getNombreServicio());
        etDesc.setText(servicio.getDescripcion());
        etCosto.setText(String.valueOf(servicio.getCosto()));
        etDuracion.setText(String.valueOf(servicio.getDuracionMinutos()));

        // Seleccionar la categoría que ya tenía el servicio
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equals(servicio.getCategoria())) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }

        btnGuardar.setText("ACTUALIZAR");

        btnGuardar.setOnClickListener(v -> {
            // ... misma lógica de validación que en registro ...
            servicio.setNombreServicio(etNombre.getText().toString());
            servicio.setCategoria(spinnerCategoria.getSelectedItem().toString());
            // ... (restos de sets) ...

            db.collection("servicios").document(servicio.getIdServicio())
                    .set(servicio.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        mostrarVistaServicios();
                    });
        });

        dialog.show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}