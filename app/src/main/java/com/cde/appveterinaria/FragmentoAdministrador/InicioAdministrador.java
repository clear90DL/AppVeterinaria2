package com.cde.appveterinaria.FragmentoAdministrador;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cde.appveterinaria.Modelos.Cita;
import com.cde.appveterinaria.Modelos.CitaAdapter;
import com.cde.appveterinaria.Modelos.Mascota;
import com.cde.appveterinaria.Modelos.MascotaAdapter;
import com.cde.appveterinaria.Modelos.Servicio;
import com.cde.appveterinaria.Modelos.ServicioAdapter;
import com.cde.appveterinaria.Modelos.Usuario;
import com.cde.appveterinaria.Modelos.UsuarioAdapter;
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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InicioAdministrador extends AppCompatActivity{
    private SwipeRefreshLayout swipeRefresh; // <-- Agrega esta línea

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private MaterialToolbar toolbar;
    private FrameLayout contenedorVistas;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String horaSeleccionadaCita;
    // --- AGREGAR ESTAS VARIABLES ---
    private Date fechaSeleccionadaCita;
    private List<String> uidsClientes = new ArrayList<>();
    private List<String> nombresClientes = new ArrayList<>();
    private List<String> idsMascotasCita = new ArrayList<>();
    private List<String> nombresMascotasCita = new ArrayList<>();


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

        // AGREGA ESTA LÍNEA AQUÍ:
        mostrarVistaPerfil();

        // Opcional: Marcar el ítem de perfil en el menú lateral para que coincida visualmente
        navigationView.setCheckedItem(R.id.nav_perfil);
        cargarEstadisticasEnTiempoReal();

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
        swipeRefresh = findViewById(R.id.swipeRefresh); // Si agregaste el SwipeRefreshLayout

    }

    // Agrega este método
    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                // Recargar estadísticas
                cargarEstadisticasEnTiempoReal();

                // Detener el indicador de carga después de 2 segundos
                new Handler().postDelayed(() -> {
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                }, 2000);
            });
        }
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
        // Limpiar el contenedor
        contenedorVistas.removeAllViews();

        // Inflar la vista de inicio
        View inicioView = getLayoutInflater().inflate(R.layout.inicio_administrador, contenedorVistas, false);
        contenedorVistas.addView(inicioView);

        // Actualizar título
        toolbar.setTitle("Panel Administrador");

        // Marcar el ítem en el menú
        navigationView.setCheckedItem(R.id.nav_inicio_administrador);

        // Cargar estadísticas
        cargarEstadisticasEnTiempoReal();
    }
    private void mostrarVistaPerfil() {
        contenedorVistas.removeAllViews();
        View perfilView = getLayoutInflater().inflate(R.layout.dialog_perfil, contenedorVistas);
        toolbar.setTitle("Mi Perfil");

        //Enlazar componentes
        perfilView.findViewById(R.id.btnCambiarPasswordPerfil).setOnClickListener(v -> mostrarDialogoCambiarContraseña());
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
        View vista = getLayoutInflater().inflate(R.layout.gestion_usuarios_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Usuarios");

        RecyclerView rvUsuarios = vista.findViewById(R.id.rvUsuariosAdmin);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        List<Usuario> listaUsuarios = new ArrayList<>();
        // Instanciamos el adaptador que definimos anteriormente
        UsuarioAdapter adapter = new UsuarioAdapter(listaUsuarios);
        rvUsuarios.setAdapter(adapter);

        // Cargar usuarios desde Firestore en tiempo real
        db.collection("usuarios")
                .orderBy("nombre")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        listaUsuarios.clear();
                        for (DocumentSnapshot doc : value) {
                            Usuario u = doc.toObject(Usuario.class);
                            if (u != null) {
                                listaUsuarios.add(u);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        // Acción del botón agregar
        vista.findViewById(R.id.btnAgregarUsuarioAdmin).setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo registro de nuevo cliente...", Toast.LENGTH_SHORT).show();
            mostrarDialogoRegistroDesdeAdmin();
        });
    }

    private void mostrarVistaCitas() {
        contenedorVistas.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.gestion_citas_administrador, contenedorVistas);
        toolbar.setTitle("Gestión de Citas");
        configurarVistaCitas(view); // <-- Agrega esta línea
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
    private void mostrarDialogoRegistroDesdeAdmin() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // APUNTAMOS AL NUEVO XML
        dialog.setContentView(R.layout.registro_usuario_admin);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // Referencias con los nuevos IDs del XML
        EditText etNombre = dialog.findViewById(R.id.etNombreAdmin);
        EditText etCorreo = dialog.findViewById(R.id.etCorreoAdmin);
        EditText etPass = dialog.findViewById(R.id.etPassAdmin);
        EditText etConfirm = dialog.findViewById(R.id.etConfirmPassAdmin);
        Spinner spinnerRol = dialog.findViewById(R.id.spinnerRolAdmin);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarUsuarioAdmin);
        // Configurar Spinner
        String[] opciones = {"cliente", "admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spinnerRol.setAdapter(adapter);
        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();
            String rol = spinnerRol.getSelectedItem().toString();
            if (validarCamposRegistro(nombre, correo, pass, confirm)) {
                ejecutarRegistroAdmin(nombre, correo, pass, rol, dialog);
            }
        });
        dialog.show();
    }
    // Cambiamos la firma para recibir el 'rol' del Spinner
    private void ejecutarRegistroAdmin(String nombre, String correo, String password, String rol, Dialog dialog) {
        Toast.makeText(this, "Registrando " + rol + "...", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = task.getResult().getUser();
                        if (newUser != null) {
                            // Estructura de datos para Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", newUser.getUid());
                            userMap.put("nombre", nombre);
                            userMap.put("email", correo);
                            userMap.put("rol", rol); // <-- USAMOS EL ROL DINÁMICO AQUÍ
                            userMap.put("activo", true);
                            userMap.put("emailVerificado", false);
                            userMap.put("fechaRegistroFormateada", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                            db.collection("usuarios").document(newUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "✅ " + rol + " registrado correctamente", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                        // El cierre de sesión es necesario porque Firebase Auth
                                        // cambia automáticamente al nuevo usuario creado.
                                        mAuth.signOut();

                                        // Redirigir al Login para que el admin regrese o el usuario entre
                                        Intent intent = new Intent(this, com.cde.appveterinaria.Principal.MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
    // 3. Método de validación (El mismo de tu MainActivity)
    private boolean validarCamposRegistro(String nombre, String correo, String password, String confirmacion) {
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmacion)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
    private void cargarClientesEnSpinner(Spinner spinner) {
        db.collection("usuarios")
                .whereEqualTo("rol", "cliente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    nombresClientes.clear();
                    uidsClientes.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        String uid = doc.getId(); // El ID del documento

                        nombresClientes.add(nombre != null ? nombre : "Usuario sin nombre");
                        uidsClientes.add(uid);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, nombresClientes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al cargar clientes", e));
    }
    // Método 2: Muestra el formulario de registro (Sustituye la lista)
    private void mostrarFormularioRegistroMascota() {
        contenedorVistas.removeAllViews();
        View vista = getLayoutInflater().inflate(R.layout.gestion_mascota_administrador, contenedorVistas, false);
        contenedorVistas.addView(vista);
        toolbar.setTitle("Nueva Mascota");
        // Referencias
        Spinner spinnerClientes = vista.findViewById(R.id.spinnerSeleccionarCliente);
        EditText etNombre = vista.findViewById(R.id.etNombreMascota2);
        EditText etEspecie = vista.findViewById(R.id.etEspecieMascota2);
        EditText etRaza = vista.findViewById(R.id.etRazaMascota3);
        EditText etEdad = vista.findViewById(R.id.etEdadMascota3);
        Spinner spinnerUnidad = vista.findViewById(R.id.spinnerUnidadEdad);
        RadioGroup rgSexo = vista.findViewById(R.id.rgSexoMascota3);
        EditText etColor = vista.findViewById(R.id.etColorMascota3);
        Button btnRegistrar = vista.findViewById(R.id.btnRegistrarMascota2);
        Button btnCancelar = vista.findViewById(R.id.btnCancelarMascota2);
        // Configuración inicial
        String[] unidades = {"Años", "Meses"};
        spinnerUnidad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unidades));
        cargarClientesEnSpinner(spinnerClientes);
        btnRegistrar.setOnClickListener(v -> {
            if (uidsClientes.isEmpty() || spinnerClientes.getSelectedItem() == null) {
                Toast.makeText(this, "Selecciona un cliente", Toast.LENGTH_SHORT).show();
                return;
            }
            String nombreM = etNombre.getText().toString().trim();
            if (nombreM.isEmpty()) { etNombre.setError("Requerido"); return; }
            // 1. Obtener tanto el ID como el NOMBRE del cliente seleccionado
            int pos = spinnerClientes.getSelectedItemPosition();
            String idUsuarioDueño = uidsClientes.get(pos);
            String nombreDelDueño = nombresClientes.get(pos); // <--- OBTENEMOS EL NOMBRE AQUÍ
            String sexo = (rgSexo.getCheckedRadioButtonId() == R.id.rbMacho) ? "Macho" : "Hembra";
            Map<String, Object> mascota = new HashMap<>();
            mascota.put("nombre", nombreM);
            mascota.put("especie", etEspecie.getText().toString().trim());
            mascota.put("raza", etRaza.getText().toString().trim());
            mascota.put("color", etColor.getText().toString().trim());
            mascota.put("sexo", sexo);
            // 2. Guardamos AMBOS datos en Firestore
            mascota.put("idUsuario", idUsuarioDueño);
            mascota.put("nombreDueño", nombreDelDueño); // <--- ESTO ES LO QUE LEERÁ TU ADAPTER
            mascota.put("fotoUrl", "");
            try {
                mascota.put("edad", Integer.parseInt(etEdad.getText().toString().trim()));
            } catch (Exception e) { mascota.put("edad", 0); }

            mascota.put("fechaRegistro", System.currentTimeMillis());
            mascota.put("fechaRegistroFormateada", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

            db.collection("mascotas").add(mascota)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ " + nombreM + " asignado a " + nombreDelDueño, Toast.LENGTH_SHORT).show();
                        mostrarVistaMascotas();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Si cancela, vuelve a la lista
        btnCancelar.setOnClickListener(v -> mostrarVistaMascotas());
    }
    private void mostrarFormularioRegistroCita() {
        contenedorVistas.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.registrar_cita_admin, contenedorVistas, false);
        contenedorVistas.addView(view);
        toolbar.setTitle("Agendar Nueva Cita");

        // Referencias (usando los IDs de tu nuevo XML)
        Spinner spDueño = view.findViewById(R.id.spinnerDueñoCita);
        Spinner spMascota = view.findViewById(R.id.spinnerMascotaCita);
        Spinner spServicio = view.findViewById(R.id.spinnerServicios);
        Spinner spVeterinario = view.findViewById(R.id.spinnerVeterinarios);
        EditText etFecha = view.findViewById(R.id.etFechaCita);
        EditText etHora = view.findViewById(R.id.etHoraCita);
        EditText etNotas = view.findViewById(R.id.etNotas);
        Button btnAgendar = view.findViewById(R.id.btnAgendarCita);
        Button btnCancelar = view.findViewById(R.id.btnCancelarCita);

        // Resetear variables temporales
        fechaSeleccionadaCita = null;
        horaSeleccionadaCita = null;

        // 1. Cargar dueños (clientes) en el spinner
        cargarClientesEnSpinner(spDueño);

        // 2. Cuando se selecciona un dueño, cargar sus mascotas
        spDueño.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position >= 0 && position < uidsClientes.size()) {
                    String uidDueño = uidsClientes.get(position);
                    cargarMascotasDelDueño(uidDueño, spMascota);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Limpiar mascotas cuando no hay dueño seleccionado
                idsMascotasCita.clear();
                nombresMascotasCita.clear();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(InicioAdministrador.this,
                        android.R.layout.simple_spinner_item, new ArrayList<String>());
                spMascota.setAdapter(adapter);
            }
        });

        // 3. Configurar Spinner de Servicios (desde Firestore)
        cargarServiciosEnSpinner(spServicio);

        // 4. Configurar Spinner de Veterinarios (lista estática)
        configurarSpinnerVeterinarios(spVeterinario);

        // 5. Selector de Fecha
        etFecha.setOnClickListener(v -> mostrarDatePicker(etFecha));

        // 6. Selector de Hora
        etHora.setOnClickListener(v -> mostrarTimePicker(etHora));

        // 7. Botón Agendar
        btnAgendar.setOnClickListener(v -> {
            if (validarFormularioCita(spDueño, spMascota, spServicio, etFecha, etHora)) {
                guardarCitaDesdeAdmin(spDueño, spMascota, spServicio, spVeterinario, etNotas);
            }
        });

        // 8. Botón Cancelar
        btnCancelar.setOnClickListener(v -> mostrarGestionCitas());
    }

    private void mostrarGestionCitas() {
        contenedorVistas.removeAllViews();
        // Inflamos el layout que contiene el RecyclerView y el botón "Agregar"
        View view = getLayoutInflater().inflate(R.layout.gestion_citas_administrador, contenedorVistas, false);
        contenedorVistas.addView(view);
        toolbar.setTitle("Gestión de Citas");

        configurarVistaCitas(view);
    }
       private void validarYGuardarCita(Spinner spD, Spinner spM, Spinner spS, Spinner spV, EditText etH, EditText etN) {
        if (fechaSeleccionadaCita == null || idsMascotasCita.isEmpty()) {
            Toast.makeText(this, "Por favor complete los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        Cita cita = new Cita(
                uidsClientes.get(spD.getSelectedItemPosition()),
                nombresClientes.get(spD.getSelectedItemPosition()),
                idsMascotasCita.get(spM.getSelectedItemPosition()),
                nombresMascotasCita.get(spM.getSelectedItemPosition()),
                spS.getSelectedItem().toString(),
                fechaSeleccionadaCita,
                etH.getText().toString(),
                spV.getSelectedItem().toString()
        );
        cita.setNotas(etN.getText().toString());

        db.collection("citas").add(cita.toMap())
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "✅ Cita agendada correctamente", Toast.LENGTH_SHORT).show();
                    mostrarGestionCitas(); // Regresa a la lista de citas
                });
    }
    private void configurarSpinnersEstaticos(Spinner spServicio, Spinner spVeterinario) {
        // Lista de Servicios
        String[] servicios = {"Consulta General", "Vacunación", "Estética Canina", "Cirugía", "Desparasitación", "Urgencia"};
        ArrayAdapter<String> adapterS = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, servicios);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServicio.setAdapter(adapterS);

        // Lista de Veterinarios
        String[] veterinarios = {"Dr. Alberto Pérez", "Dra. María García", "Dr. Ricardo Luna", "Sin asignar"};
        ArrayAdapter<String> adapterV = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, veterinarios);
        adapterV.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVeterinario.setAdapter(adapterV);
    }
    public void onEditarMascota(Mascota mascota, int position) {
        // Aquí es donde defines qué pasa cuando tocas "Editar"
        Toast.makeText(this, "Editando a: " + mascota.getNombre(), Toast.LENGTH_SHORT).show();

        // Por ahora, puedes abrir el formulario de registro:
        mostrarFormularioRegistroMascota();
    }

    private void cargarMascotasDelDueño(String uidDueño, Spinner spMascota) {
        idsMascotasCita.clear();
        nombresMascotasCita.clear();

        db.collection("mascotas")
                .whereEqualTo("idUsuario", uidDueño)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String idMascota = doc.getId();
                        String nombreMascota = doc.getString("nombre");
                        String especie = doc.getString("especie");

                        if (nombreMascota != null) {
                            idsMascotasCita.add(idMascota);
                            String texto = especie != null ?
                                    nombreMascota + " (" + especie + ")" : nombreMascota;
                            nombresMascotasCita.add(texto);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, nombresMascotasCita);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spMascota.setAdapter(adapter);

                    if (nombresMascotasCita.isEmpty()) {
                        Toast.makeText(this, "Este cliente no tiene mascotas registradas",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CitasAdmin", "Error al cargar mascotas: " + e.getMessage());
                });
    }

    private void cargarServiciosEnSpinner(Spinner spinner) {
        db.collection("servicios")
                .orderBy("nombreServicio")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> servicios = new ArrayList<>();
                    servicios.add("Seleccionar servicio");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombreServicio");
                        String descripcion = doc.getString("descripcion");
                        Double costo = doc.getDouble("costo");

                        if (nombre != null) {
                            String texto = costo != null ?
                                    nombre + " - S/" + costo : nombre;
                            servicios.add(texto);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, servicios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("CitasAdmin", "Error al cargar servicios: " + e.getMessage());
                    // Si falla, mostrar lista básica
                    String[] serviciosBasicos = {
                            "Seleccionar servicio",
                            "Consulta General",
                            "Vacunación",
                            "Desparasitación",
                            "Cirugía",
                            "Estética"
                    };
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, serviciosBasicos);
                    spinner.setAdapter(adapter);
                });
    }

    private void configurarSpinnerVeterinarios(Spinner spinner) {
        String[] veterinarios = {
                "Sin asignar",
                "Dr. Juan García",
                "Dra. María López",
                "Dr. Carlos Méndez",
                "Dra. Ana Torres",
                "Dr. Pedro Sánchez"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, veterinarios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void mostrarDatePicker(EditText etFecha) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    fechaSeleccionadaCita = new Date(selectedYear - 1900, selectedMonth, selectedDay);
                    String fechaFormateada = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFecha.setText(fechaFormateada);
                }, year, month, day);

        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarTimePicker(EditText etHora) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    horaSeleccionadaCita = String.format(Locale.getDefault(),
                            "%02d:%02d", selectedHour, selectedMinute);
                    etHora.setText(horaSeleccionadaCita);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private boolean validarFormularioCita(Spinner spDueño, Spinner spMascota,
                                          Spinner spServicio, EditText etFecha, EditText etHora) {

        if (spDueño.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, "Selecciona un dueño", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spMascota.getSelectedItemPosition() < 0 || idsMascotasCita.isEmpty()) {
            Toast.makeText(this, "Selecciona una mascota", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spServicio.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, "Selecciona un servicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etFecha.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etHora.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Selecciona una hora", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fechaSeleccionadaCita == null) {
            Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void guardarCitaDesdeAdmin(Spinner spDueño, Spinner spMascota, Spinner spServicio,
                                       Spinner spVeterinario, EditText etNotas) {

        int posDueño = spDueño.getSelectedItemPosition();
        int posMascota = spMascota.getSelectedItemPosition();

        String uidDueño = uidsClientes.get(posDueño - 1); // -1 porque el primer item es "Seleccionar"
        String nombreDueño = nombresClientes.get(posDueño - 1);
        String idMascota = idsMascotasCita.get(posMascota);
        String nombreMascota = nombresMascotasCita.get(posMascota);
        String servicio = spServicio.getSelectedItem().toString();
        String veterinario = spVeterinario.getSelectedItem().toString();
        String notas = etNotas.getText().toString().trim();

        // Obtener el email del dueño
        db.collection("usuarios").document(uidDueño).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String emailDueño = documentSnapshot.getString("email");

                    // Crear objeto Cita
                    Cita cita = new Cita(
                            uidDueño,
                            emailDueño,
                            idMascota,
                            nombreMascota,
                            servicio,
                            fechaSeleccionadaCita,
                            horaSeleccionadaCita,
                            veterinario
                    );
                    cita.setNotas(notas);
                    cita.setNombreUsuario(nombreDueño);
                    cita.setEstado("pendiente");

                    // Guardar en Firestore
                    db.collection("citas")
                            .add(cita.toMap())
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "✅ Cita agendada correctamente",
                                        Toast.LENGTH_SHORT).show();
                                mostrarGestionCitas();

                                // Aquí podrías enviar una notificación al cliente
                                enviarNotificacionCliente(emailDueño, nombreDueño,
                                        nombreMascota, servicio, fechaSeleccionadaCita);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "❌ Error al guardar cita: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                Log.e("CitasAdmin", "Error: ", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos del cliente", Toast.LENGTH_SHORT).show();
                });
    }

    private void enviarNotificacionCliente(String emailCliente, String nombreCliente,
                                           String nombreMascota, String servicio, Date fecha) {
        // Aquí puedes implementar el envío de notificación
        // Por ejemplo, enviar un email o guardar una notificación en Firestore

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fechaStr = sdf.format(fecha);

        // Crear notificación en Firestore
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("destinatarioEmail", emailCliente);
        notificacion.put("titulo", "Nueva Cita Agendada");
        notificacion.put("mensaje", "Hola " + nombreCliente +
                ", se ha agendado una cita para " + nombreMascota +
                " el servicio de " + servicio + " para el " + fechaStr);
        notificacion.put("fecha", new Date());
        notificacion.put("leida", false);

        db.collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notificacion", "Notificación guardada para " + emailCliente);
                });

        Toast.makeText(this, "Notificación enviada al cliente", Toast.LENGTH_SHORT).show();
    }

    // Método para mostrar la lista de citas


    private void configurarVistaCitas(View view) {
        RecyclerView rvCitas = view.findViewById(R.id.rvCitasAdmin);
        TextView tvSinCitas = view.findViewById(R.id.tvSinCitas);
        Button btnAgregar = view.findViewById(R.id.btnAgregarNuevaCita);

        rvCitas.setLayoutManager(new LinearLayoutManager(this));
        List<Cita> listaCitas = new ArrayList<>();

        // Usar tu CitaAdapter existente
        CitaAdapter adapter = new CitaAdapter(this, listaCitas);
        rvCitas.setAdapter(adapter);

        // Cargar TODAS las citas
        db.collection("citas")
                .orderBy("fechaCita", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CitasAdmin", "Error: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        listaCitas.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Cita cita = doc.toObject(Cita.class);
                                if (cita != null) {
                                    cita.setIdCita(doc.getId());
                                    listaCitas.add(cita);
                                }
                            } catch (Exception e) {
                                Log.e("CitasAdmin", "Error al parsear cita", e);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // Mostrar/ocultar mensaje
                        if (listaCitas.isEmpty()) {
                            rvCitas.setVisibility(View.GONE);
                            if (tvSinCitas != null) tvSinCitas.setVisibility(View.VISIBLE);
                        } else {
                            rvCitas.setVisibility(View.VISIBLE);
                            if (tvSinCitas != null) tvSinCitas.setVisibility(View.GONE);
                        }
                    }
                });

        // Botón para agregar nueva cita
        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(v -> mostrarFormularioRegistroCita());
        }
    }
    // En tu InicioAdministrador, agrega estos métodos:

    // Método para mostrar diálogo de edición de mascota (Administrador)
    private void mostrarDialogoEditarMascotaAdmin(Mascota mascota, int position,
                                                  List<Mascota> listaMascotas,
                                                  MascotaAdapter adapter) {
        Dialog dialog = new Dialog(this, R.style.DialogoTransparente);
        dialog.setContentView(R.layout.editar_mascota_dialog);

        // Configurar el diálogo
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.CENTER);
        }

        dialog.setCancelable(true);

        // Referencias a los campos
        EditText etNombre = dialog.findViewById(R.id.etEditarNombreMascota);
        EditText etEspecie = dialog.findViewById(R.id.etEditarEspecieMascota);
        EditText etRaza = dialog.findViewById(R.id.etEditarRazaMascota);
        EditText etEdad = dialog.findViewById(R.id.etEditarEdadMascota);
        RadioGroup rgSexo = dialog.findViewById(R.id.rgEditarSexoMascota);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelarEditar);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarEditar);

        // Cargar datos actuales
        etNombre.setText(mascota.getNombre());
        etEspecie.setText(mascota.getEspecie());
        etRaza.setText(mascota.getRaza());
        etEdad.setText(String.valueOf(mascota.getEdad()));

        // Seleccionar sexo actual
        if (mascota.getSexo().equals("Macho")) {
            rgSexo.check(R.id.rbEditarMacho);
        } else {
            rgSexo.check(R.id.rbEditarHembra);
        }

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String especie = etEspecie.getText().toString().trim();
            String raza = etRaza.getText().toString().trim();
            String edadStr = etEdad.getText().toString().trim();

            // Validaciones
            if (nombre.isEmpty()) {
                etNombre.setError("Ingresa el nombre");
                etNombre.requestFocus();
                return;
            }

            if (especie.isEmpty()) {
                etEspecie.setError("Ingresa la especie");
                etEspecie.requestFocus();
                return;
            }

            if (edadStr.isEmpty()) {
                etEdad.setError("Ingresa la edad");
                etEdad.requestFocus();
                return;
            }

            int sexoId = rgSexo.getCheckedRadioButtonId();
            if (sexoId == -1) {
                Toast.makeText(this, "Selecciona el sexo", Toast.LENGTH_SHORT).show();
                return;
            }

            String sexo = sexoId == R.id.rbEditarMacho ? "Macho" : "Hembra";

            int edad;
            try {
                edad = Integer.parseInt(edadStr);
                if (edad <= 0) {
                    etEdad.setError("La edad debe ser mayor a 0");
                    etEdad.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etEdad.setError("Edad inválida");
                etEdad.requestFocus();
                return;
            }

            // Actualizar objeto mascota
            mascota.setNombre(nombre);
            mascota.setEspecie(especie);
            mascota.setRaza(raza.isEmpty() ? "No especificada" : raza);
            mascota.setEdad(edad);
            mascota.setSexo(sexo);

            // Guardar en Firestore (método específico para admin)
            actualizarMascotaEnFirestoreAdmin(mascota, position, listaMascotas, adapter, dialog);
        });

        dialog.show();
    }

    // Método para actualizar mascota en Firestore (Administrador)
    private void actualizarMascotaEnFirestoreAdmin(Mascota mascota, int position,
                                                   List<Mascota> listaMascotas,
                                                   MascotaAdapter adapter,
                                                   Dialog dialog) {
        db.collection("mascotas")
                .document(mascota.getIdMascota())
                .set(mascota.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Mascota actualizada exitosamente",
                            Toast.LENGTH_SHORT).show();

                    // Actualizar lista local
                    listaMascotas.set(position, mascota);
                    adapter.notifyItemChanged(position);

                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al actualizar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Método para confirmar eliminación (Administrador)
    private void mostrarDialogoConfirmarEliminarAdmin(Mascota mascota, int position,
                                                      List<Mascota> listaMascotas,
                                                      MascotaAdapter adapter) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Mascota")
                .setMessage("¿Estás seguro de eliminar a " + mascota.getNombre() +
                        " del cliente " + mascota.getNombreDueño() + "?")
                .setPositiveButton("ELIMINAR", (dialog, which) -> {
                    eliminarMascotaDeFirestoreAdmin(mascota, position, listaMascotas, adapter);
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    // Método para eliminar de Firestore (Administrador)
    private void eliminarMascotaDeFirestoreAdmin(Mascota mascota, int position,
                                                 List<Mascota> listaMascotas,
                                                 MascotaAdapter adapter) {
        db.collection("mascotas")
                .document(mascota.getIdMascota())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Mascota eliminada exitosamente",
                            Toast.LENGTH_SHORT).show();

                    // Eliminar de la lista local
                    listaMascotas.remove(position);
                    adapter.notifyItemRemoved(position);

                    // Verificar si quedan mascotas
                    if (listaMascotas.isEmpty()) {
                        // Aquí puedes mostrar un mensaje si quieres
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al eliminar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // Método para mostrar detalles de mascota (Administrador)
    private void mostrarDetallesMascotaAdmin(Mascota mascota) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Detalles de " + mascota.getNombre());

        // Buscar información del dueño para mostrar más detalles
        db.collection("usuarios").document(mascota.getIdUsuario())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String nombreDueño = "No encontrado";
                    String emailDueño = "No encontrado";

                    if (documentSnapshot.exists()) {
                        nombreDueño = documentSnapshot.getString("nombre");
                        emailDueño = documentSnapshot.getString("email");
                    }

                    String detalles = "Dueño: " + nombreDueño + "\n" +
                            "Email dueño: " + emailDueño + "\n\n" +
                            "Especie: " + mascota.getEspecie() + "\n" +
                            "Raza: " + mascota.getRaza() + "\n" +
                            "Edad: " + mascota.getEdad() + " años\n" +
                            "Sexo: " + mascota.getSexo() + "\n";


                    builder.setMessage(detalles);
                    builder.setPositiveButton("Cerrar", null);

                    // El admin siempre puede editar
                    builder.setNeutralButton("Editar", (dialog, which) -> {
                        // Necesitarías encontrar la posición en la lista actual
                        // Podrías pasar el adaptador y lista como parámetros
                    });

                    builder.show();
                });
    }
    // Reemplaza tu método mostrarVistaMascotas() con esta versión mejorada
    private void mostrarVistaMascotas() {
        contenedorVistas.removeAllViews();
        View vistaPrincipal = getLayoutInflater().inflate(R.layout.registrar_mascota_administrador,
                contenedorVistas, false);
        contenedorVistas.addView(vistaPrincipal);
        toolbar.setTitle("Panel de Mascotas");

        RecyclerView rvMascotas = vistaPrincipal.findViewById(R.id.rvMascotasAdmin);
        rvMascotas.setLayoutManager(new LinearLayoutManager(this));

        List<Mascota> listaMascotas = new ArrayList<>();

        // Hacer el adaptador FINAL para que sea accesible desde el listener
        final MascotaAdapter[] adapterHolder = new MascotaAdapter[1];

        // Configurar el adaptador con listener completo
        MascotaAdapter adapter = new MascotaAdapter(this, listaMascotas, true,
                new MascotaAdapter.OnMascotaListener() {
                    @Override
                    public void onEditarMascota(Mascota mascota, int position) {
                        mostrarDialogoEditarMascotaAdmin(mascota, position, listaMascotas, adapterHolder[0]);
                    }

                    @Override
                    public void onEliminarMascota(Mascota mascota, int position) {
                        mostrarDialogoConfirmarEliminarAdmin(mascota, position, listaMascotas, adapterHolder[0]);
                    }

                    @Override
                    public void onMascotaClick(Mascota mascota, int position) {
                        mostrarDetallesMascotaAdmin(mascota);
                    }
                });

        // Guardar el adaptador en el holder
        adapterHolder[0] = adapter;
        rvMascotas.setAdapter(adapter);

        // Carga en tiempo real
        db.collection("mascotas")
                .orderBy("fechaRegistro", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        listaMascotas.clear();
                        for (DocumentSnapshot doc : value) {
                            Mascota m = doc.toObject(Mascota.class);
                            if (m != null) {
                                m.setIdMascota(doc.getId());
                                listaMascotas.add(m);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        // Botón para agregar nueva mascota
        Button btnAbrir = vistaPrincipal.findViewById(R.id.btnAbrirFormularioMascota2);
        if (btnAbrir != null) {
            btnAbrir.setOnClickListener(v -> mostrarFormularioRegistroMascota());
        }
    }

    private void cargarEstadisticasEnTiempoReal() {
        // Verificar que estamos en la vista correcta
        if (!toolbar.getTitle().toString().equals("Panel Administrador")) {
            return;
        }

        // Buscar las vistas del layout incluido
        View inicioView = contenedorVistas.findViewById(R.id.tvTotalUsuarios);
        if (inicioView == null) {
            // Si no encuentra las vistas, es porque estamos en otra pantalla
            return;
        }

        // 1. Total de Usuarios
        db.collection("usuarios")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        TextView tvUsuarios = contenedorVistas.findViewById(R.id.tvTotalUsuarios);
                        if (tvUsuarios != null) {
                            tvUsuarios.setText("👥 Usuarios: " + value.size());
                        }
                    }
                });

        // 2. Total de Mascotas
        db.collection("mascotas")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        TextView tvMascotas = contenedorVistas.findViewById(R.id.tvTotalMascotas);
                        if (tvMascotas != null) {
                            tvMascotas.setText("🐾 Mascotas: " + value.size());
                        }
                    }
                });

        // 3. Citas de hoy
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);

        db.collection("citas")
                .whereGreaterThanOrEqualTo("fechaCita", hoy.getTime())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        TextView tvCitasHoy = contenedorVistas.findViewById(R.id.tvCitasHoy);
                        if (tvCitasHoy != null) {
                            tvCitasHoy.setText("📅 Citas hoy: " + value.size());
                        }
                    }
                });

        // 4. Citas pendientes
        db.collection("citas")
                .whereEqualTo("estado", "pendiente")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        TextView tvPendientes = contenedorVistas.findViewById(R.id.tvCitasPendientes);
                        if (tvPendientes != null) {
                            tvPendientes.setText("⏰ Pendientes: " + value.size());
                        }
                    }
                });
    }

    }

