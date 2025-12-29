package com.cde.appveterinaria.FragmentoCliente;
import com.cde.appveterinaria.Modelos.CitaAdapter;
import com.cde.appveterinaria.Modelos.Servicio;
import com.cde.appveterinaria.Modelos.ServicioAdapter;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.cde.appveterinaria.FragmentoAdministrador.InicioAdministrador;
import com.cde.appveterinaria.Modelos.Cita;
import com.cde.appveterinaria.Modelos.Mascota;
import com.cde.appveterinaria.Principal.LoginActivity;
import com.cde.appveterinaria.Principal.MainActivity;
import com.cde.appveterinaria.Modelos.MascotaAdapter;
import com.cde.appveterinaria.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
// Importaciones adicionales
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.app.Dialog;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;

public class InicioCliente extends AppCompatActivity{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private MaterialToolbar toolbar;
    private FrameLayout contenedorVistas;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel_cliente);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar autenticación
        verificarAutenticacion();

        initViews();
        setupToolbar();
        setupDrawerLayout();
        setupNavigationView();

        // AGREGA ESTA LÍNEA AQUÍ:
        mostrarVistaPerfil();

        // Opcional: Marcar el ítem de perfil en el menú lateral para que coincida visualmente
        navigationView.setCheckedItem(R.id.nav_perfil);
    }

    private void verificarAutenticacion() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.getEmail() != null &&
                currentUser.getEmail().equals("admin@gmail.com")) {
            Toast.makeText(this, "Redirigiendo al panel de administrador",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, InicioAdministrador.class);
            startActivity(intent);
            finish();
        } else if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar2);
        contenedorVistas = findViewById(R.id.contenedorVistas);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
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

            if (id == R.id.nav_inicio_cliente) {
                mostrarVistaInicio();
            } else if (id == R.id.nav_perfil) {
                mostrarVistaPerfil();
            } else if (id == R.id.nav_registrar_mascota_cliente) {
                mostrar_registro_mascota();
            } else if (id == R.id.nav_mis_mascotas) {
                mostrar_ver_mascotas();
            } else if (id == R.id.nav_solicitar_cita_cliente) {
                // CORRECCIÓN: Pasamos null porque el cliente entró desde el menú,
                // no desde una "tarjeta" de servicio específica.
                mostrar_cita_cliente(null);
            } else if (id == R.id.ver_mis_citas_cliente) {
                mostrar_ver_citas_disponibles();
            } else if (id == R.id.ServiciosDisponibles_cliente) {
                mostarServicioDisponible();
            } else if (id == R.id.btnCerrarSesion) {
                mostrarDialogoConfirmarCerrarSesion();
                // Dentro de setupNavigationView
            } else if (id == R.id.cambiar_contraseña) {
                mostrarDialogoCambiarContraseña(); // Cambiado de cambiarContraseña() a este
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
    private void mostrarVistaPerfil() {
        contenedorVistas.removeAllViews();
        View perfilView = getLayoutInflater().inflate(R.layout.dialog_perfil, contenedorVistas);
        toolbar.setTitle("Mi Perfil");

        //Enlazar componentes
        perfilView.findViewById(R.id.btnCambiarPasswordPerfil).setOnClickListener(v -> mostrarDialogoCambiarContraseña());

        // Cargar datos del perfil
        cargarDatosPerfil(perfilView);
    }
    // Métodos para mostrar vistas
    private void mostrarVistaInicio() {
        contenedorVistas.removeAllViews();
        View vista = getLayoutInflater().inflate(R.layout.inicio_cliente, contenedorVistas);
        toolbar.setTitle("Panel Cliente");

        // 1. Enlazar componentes del XML
        TextView tvNombre = vista.findViewById(R.id.tvClientName);
        TextView tvEmail = vista.findViewById(R.id.tvClientEmail);
        TextView tvMascotasContador = vista.findViewById(R.id.tvPetCount);

        // 2. Obtener datos del usuario actual (Auth)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            String idUsuario = user.getUid();

            // 3. Escuchar cambios en las MASCOTAS (Tiempo real)
            db.collection("mascotas")
                    .whereEqualTo("idUsuario", idUsuario)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;
                        if (value != null) {
                            int cantidad = value.size();
                            tvMascotasContador.setText(cantidad + (cantidad == 1 ? " mascota registrada" : " mascotas registradas"));
                        }
                    });


        }

        // 5. Configurar clics de botones
        vista.findViewById(R.id.cardMyPets).setOnClickListener(v -> mostrar_ver_mascotas());
        vista.findViewById(R.id.cardSchedule).setOnClickListener(v -> mostrar_cita_cliente(null));
          }
    // Método auxiliar para no amontonar todo el código arriba
    private void cargarDatosRestantes(String uid, TextView tvNombre, TextView tvCita, TextView tvDetalle) {
        // Obtener nombre desde la colección usuarios
        db.collection("usuarios").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvNombre.setText("Bienvenido, " + doc.getString("nombre"));
            }
        });

        // Aquí podrías añadir la lógica para buscar la cita más cercana si lo deseas
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
            buscarDatosAdicionalesEnFirestore(user.getUid(), view);

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
                            // Si hay nombre en Firestore, actualizar
                            String nombreFirestore = document.getString("nombre");
                            if (nombreFirestore != null && !nombreFirestore.isEmpty()) {
                                TextView tvNombrePerfil = parentView.findViewById(R.id.tvNombrePerfil);
                                if (tvNombrePerfil != null) {
                                    tvNombrePerfil.setText("Nombre: " + nombreFirestore);
                                }
                            }

                            // También mostrar el rol
                            String rol = document.getString("rol");
                            if (rol != null) {
                                // Buscar TextView para el rol o crear uno si no existe
                                TextView tvRol = parentView.findViewById(R.id.tvRolPerfil);
                                if (tvRol == null) {
                                    // Si no existe el TextView, podrías agregarlo dinámicamente
                                    // o simplemente ignorarlo si no está en el layout
                                } else {
                                    tvRol.setText("Rol: " + rol);
                                }
                            }
                        }
                    }
                });
    }

    private void mostrar_registro_mascota() {
        contenedorVistas.removeAllViews();
        View registroView = getLayoutInflater().inflate(R.layout.registrar_mascota_cliente, contenedorVistas);
        toolbar.setTitle("Registro de Mascotas");

        // ¡IMPORTANTE! Llamar al método para configurar el formulario
        configurarFormularioRegistroMascota(registroView);
    }
    private List<String> idsMascotasCita = new ArrayList<>();
    private List<String> nombresMascotasCita = new ArrayList<>();
    private Date fechaSeleccionadaCita;
    private void mostrar_cita_cliente(String servicioPreseleccionado) {
        contenedorVistas.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.registrar_cita_cliente, contenedorVistas);
        toolbar.setTitle("Solicitar Cita");

        // Buscamos el Spinner de servicios en el layout inflado
        Spinner spinnerServicios = view.findViewById(R.id.spinnerServicios);

        // Si venimos de la lista de servicios, guardamos el nombre en el Tag
        if (servicioPreseleccionado != null && spinnerServicios != null) {
            spinnerServicios.setTag(servicioPreseleccionado);
        }

        // Llamamos a la configuración del formulario (donde se cargan los datos de Firebase)
        configurarFormularioCita(view);
    }

    private void mostrarDialogoCambiarContraseña() {
        contenedorVistas.removeAllViews();
        View vista = getLayoutInflater().inflate(R.layout.dialogo_cambiar_password, contenedorVistas);
        toolbar.setTitle("Cambio de contraseña");

        // 1. Vincular con los IDs de TU XML
        EditText etActual = vista.findViewById(R.id.txtContraseñaActual);
        EditText etNueva = vista.findViewById(R.id.txtNuevaContraseña);
        EditText etConfirmar = vista.findViewById(R.id.txtConfirmarContraseña);
        Button btnCambiar = vista.findViewById(R.id.btnCambiarPassword);
        Button btnCancelar = vista.findViewById(R.id.btnCancelarCambiarPassword);

        // 2. Lógica del botón Cambiar
        btnCambiar.setOnClickListener(v -> {
            String actual = etActual.getText().toString().trim();
            String nueva = etNueva.getText().toString().trim();
            String confirmar = etConfirmar.getText().toString().trim();

            if (validarCamposCambioContraseña(actual, nueva, confirmar)) {
                ejecutarCambioPassword(actual, nueva);
            }
        });

        // 3. Lógica del botón Cancelar
        btnCancelar.setOnClickListener(v -> mostrarVistaInicio());
    }


  /*  private void mostarServicioDisponible() {
        contenedorVistas.removeAllViews();
        getLayoutInflater().inflate(R.layout.ver_servicios_disponibles, contenedorVistas);
        toolbar.setTitle("Servicios Disponibles");
        Toast.makeText(this, "Servicios disponibles", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            mostrarDialogoSalirApp();
        }
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

    private void configurarFormularioRegistroMascota(View registroView) {
        // 1. OBTENER REFERENCIAS CORRECTAMENTE usando el View inflado
        EditText etNombre = registroView.findViewById(R.id.etNombreMascota);
        EditText etEspecie = registroView.findViewById(R.id.etEspecieMascota);
        EditText etRaza = registroView.findViewById(R.id.etRazaMascota);
        EditText etEdad = registroView.findViewById(R.id.etEdadMascota);
        EditText etColor = registroView.findViewById(R.id.etColorMascota);
        Spinner spinnerUnidadEdad = registroView.findViewById(R.id.spinnerUnidadEdad);
        RadioGroup rgSexo = registroView.findViewById(R.id.rgSexoMascota);
        Button btnRegistrar = registroView.findViewById(R.id.btnRegistrarMascota);
        Button btnCancelar = registroView.findViewById(R.id.btnCancelarMascota);

        // 2. CONFIGURAR SPINNER
        String[] unidadesEdad = {"Meses", "Años"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unidadesEdad);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidadEdad.setAdapter(adapter);
        spinnerUnidadEdad.setSelection(1); // Seleccionar "Años" por defecto

        // 3. AGREGAR DEBUG: Verificar que los botones no son null
        if (btnRegistrar == null) {
            Toast.makeText(this, "ERROR: btnRegistrar es null", Toast.LENGTH_LONG).show();
            return;
        }

        if (btnCancelar == null) {
            Toast.makeText(this, "ERROR: btnCancelar es null", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. LISTENER PARA BOTÓN REGISTRAR
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InicioCliente.this, "Botón Registrar presionado",
                        Toast.LENGTH_SHORT).show();

                // Obtener valores
                String nombre = etNombre.getText().toString().trim();
                String especie = etEspecie.getText().toString().trim();
                String raza = etRaza.getText().toString().trim();
                String edadStr = etEdad.getText().toString().trim();
                String color = etColor.getText().toString().trim();
                String unidadEdad = spinnerUnidadEdad.getSelectedItem().toString();

                // Validar campos requeridos
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

                // Validar sexo
                int sexoId = rgSexo.getCheckedRadioButtonId();
                if (sexoId == -1) {
                    Toast.makeText(InicioCliente.this, "Selecciona el sexo",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String sexo = sexoId == R.id.rbMacho ? "Macho" : "Hembra";

                // Convertir edad a número
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

                // Registrar mascota
                registrarMascotaEnFirestore(nombre, especie, raza, edad, unidadEdad, sexo, color);
            }
        });

        // 5. LISTENER PARA BOTÓN CANCELAR
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarVistaInicio();
            }
        });
    }

    private void registrarMascotaEnFirestore(String nombreMascota, String especie, String raza,
                                             int edad, String unidadEdad, String sexo, String color) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // NOTA: Cambiamos "Usuarios" por "usuarios" (minúscula)
        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {

                    // 1. Declaramos una variable temporal para el nombre
                    String nombreObtenido = "Cliente Desconocido";

                    if (documentSnapshot.exists()) {
                        nombreObtenido = documentSnapshot.getString("nombre");
                    }

                    // 2. CREAMOS UNA VARIABLE FINAL (Esto quita el error del Lambda)
                    final String nombreFinal = nombreObtenido;

                    // 3. Crear el objeto Mascota usando nombreFinal
                    Mascota mascota = new Mascota(nombreMascota, especie, raza, edad, user.getUid(), sexo);
                    mascota.setNombreDueño(nombreFinal);


                    String mascotaId = db.collection("mascotas").document().getId();
                    mascota.setIdMascota(mascotaId);

                    db.collection("mascotas").document(mascotaId).set(mascota.toMap())
                            .addOnSuccessListener(aVoid -> {
                                // USAMOS nombreFinal AQUÍ TAMBIÉN
                                Toast.makeText(InicioCliente.this, "✅ Mascota de " + nombreFinal + " registrada", Toast.LENGTH_SHORT).show();
                                mostrar_ver_mascotas();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(InicioCliente.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    // Método para mostrar la vista de mascotas
    private void mostrar_ver_mascotas() {
        contenedorVistas.removeAllViews();
        View verMascotasView = getLayoutInflater().inflate(R.layout.ver_mascotas_cliente, contenedorVistas);
        toolbar.setTitle("Mis Mascotas");

        configurarVistaMascotas(verMascotasView);
    }
    // Método para cargar mascotas desde Firestore
    private void cargarMascotasDesdeFirebase(List<Mascota> listaMascotas,
                                             MascotaAdapter adapter,
                                             TextView tvSinMascotas,
                                             RecyclerView rvMascotas) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Filtramos para que el cliente solo vea sus propias mascotas
        db.collection("mascotas")
                .whereEqualTo("idUsuario", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaMascotas.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Mascota mascota = document.toObject(Mascota.class);
                        if (mascota != null) {
                            mascota.setIdMascota(document.getId());
                            listaMascotas.add(mascota);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (listaMascotas.isEmpty()) {
                        tvSinMascotas.setVisibility(View.VISIBLE);
                        rvMascotas.setVisibility(View.GONE);
                    } else {
                        tvSinMascotas.setVisibility(View.GONE);
                        rvMascotas.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_FIRESTORE", "Error al cargar: " + e.getMessage());
                });
    }

    private void mostrarDialogoEditarMascota(Mascota mascota, int position,
                                             List<Mascota> listaMascotas,
                                             MascotaAdapter adapter) {
        Dialog dialog = new Dialog(this, R.style.DialogoTransparente); // USAR EL ESTILO
        dialog.setContentView(R.layout.editar_mascota_dialog);

        // Configurar el diálogo
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.CENTER); // Centrado o BOTTOM para abajo

            // Agregar margen para que no toque los bordes
            WindowManager.LayoutParams params = window.getAttributes();
            params.horizontalMargin = 0.05f; // 5% de margen horizontal
            window.setAttributes(params);
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

            // Guardar en Firestore
            actualizarMascotaEnFirestore(mascota, position, listaMascotas, adapter, dialog);
        });

        dialog.show();
    }


     // Método para configurar el formulario de citas
    private void configurarFormularioCita(View citaView) {
        Spinner spinnerMascotas = citaView.findViewById(R.id.spinnerMascotas);
        Spinner spinnerServicios = citaView.findViewById(R.id.spinnerServicios); // Este será dinámico
        Spinner spinnerVeterinarios = citaView.findViewById(R.id.spinnerVeterinarios);
        EditText etFechaCita = citaView.findViewById(R.id.etFechaCita);
        EditText etHoraCita = citaView.findViewById(R.id.etHoraCita);
        EditText etNotas = citaView.findViewById(R.id.etNotas);
        Button btnCancelar = citaView.findViewById(R.id.btnCancelarCita);
        Button btnAgendar = citaView.findViewById(R.id.btnAgendarCita);

        // Cargar mascotas del usuario
        cargarMascotasEnSpinner(spinnerMascotas);

// NUEVO: Cargar servicios desde Firestore
        cargarServiciosDesdeFirestore(spinnerServicios);


        // Configurar spinner de veterinarios
        String[] veterinarios = {
                "Sin preferencia",
                "Dr. Juan García",
                "Dra. María López",
                "Dr. Carlos Méndez",
                "Dra. Ana Torres"
        };
        ArrayAdapter<String> adapterVeterinarios = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, veterinarios);
        adapterVeterinarios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVeterinarios.setAdapter(adapterVeterinarios);

        // Configurar selector de fecha
        etFechaCita.setOnClickListener(v -> mostrarDatePicker(etFechaCita));

        // Configurar selector de hora
        etHoraCita.setOnClickListener(v -> mostrarTimePicker(etHoraCita));

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> mostrarVistaInicio());

        // Botón Agendar
        btnAgendar.setOnClickListener(v -> {
            registrarCita(spinnerMascotas, spinnerServicios, spinnerVeterinarios,
                    etFechaCita, etHoraCita, etNotas);
        });
    }
    private void cargarServiciosDesdeFirestore(Spinner spinner) {
        db.collection("servicios")
                .get() // O .whereEqualTo("disponible", true) si tienes ese campo
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombresServicios = new ArrayList<>();
                    nombresServicios.add("Seleccionar servicio");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombreServicio");
                        if (nombre != null) nombresServicios.add(nombre);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, nombresServicios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    // --- LÓGICA DE AUTO-SELECCIÓN ---
                    String servicioPreseleccionado = (String) spinner.getTag();
                    if (servicioPreseleccionado != null) {
                        int pos = adapter.getPosition(servicioPreseleccionado);
                        if (pos >= 0) {
                            spinner.setSelection(pos);
                        }
                    }
                });
    }
    // Método para cargar mascotas en el spinner
    private void cargarMascotasEnSpinner(Spinner spinner) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("mascotas")
                .whereEqualTo("idUsuario", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombresMascotas = new ArrayList<>();
                    List<String> idsMascotas = new ArrayList<>();

                    nombresMascotas.add("Seleccionar mascota");
                    idsMascotas.add("");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Mascota mascota = doc.toObject(Mascota.class);
                        if (mascota != null) {
                            nombresMascotas.add(mascota.getNombre() + " (" + mascota.getEspecie() + ")");
                            idsMascotas.add(doc.getId());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, nombresMascotas);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    // Guardar los IDs en el tag del spinner para usarlos después
                    spinner.setTag(idsMascotas);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para mostrar selector de fecha
    private void mostrarDatePicker(EditText etFecha) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    String fecha = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFecha.setText(fecha);
                    etFecha.setTag(calendar.getTime()); // Guardar Date object
                }, year, month, day);

        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Método para mostrar selector de hora
    private void mostrarTimePicker(EditText etHora) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String hora = String.format("%02d:%02d", selectedHour, selectedMinute);
                    etHora.setText(hora);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    // Método para registrar la cita
    private void registrarCita(Spinner spinnerMascotas, Spinner spinnerServicios,
                               Spinner spinnerVeterinarios, EditText etFecha,
                               EditText etHora, EditText etNotas) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validaciones
        int posMascota = spinnerMascotas.getSelectedItemPosition();
        if (posMascota == 0) {
            Toast.makeText(this, "Selecciona una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        int posServicio = spinnerServicios.getSelectedItemPosition();
        if (posServicio == 0) {
            Toast.makeText(this, "Selecciona un servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = etFecha.getText().toString().trim();
        if (fecha.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        String hora = etHora.getText().toString().trim();
        if (hora.isEmpty()) {
            Toast.makeText(this, "Selecciona una hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener datos
        @SuppressWarnings("unchecked")
        List<String> idsMascotas = (List<String>) spinnerMascotas.getTag();
        String idMascota = idsMascotas.get(posMascota);
        String nombreMascota = spinnerMascotas.getSelectedItem().toString();
        String servicio = spinnerServicios.getSelectedItem().toString();
        String veterinario = spinnerVeterinarios.getSelectedItem().toString();
        String notas = etNotas.getText().toString().trim();

        // Convertir fecha string a Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date fechaCita;
        try {
            fechaCita = sdf.parse(fecha);
        } catch (Exception e) {
            Toast.makeText(this, "Error en formato de fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Cita
        Cita cita = new Cita(
                user.getUid(),
                user.getEmail(),
                idMascota,
                nombreMascota,
                servicio,
                fechaCita,
                hora,
                veterinario
        );
        cita.setNotas(notas);

        // Guardar en Firestore
        Toast.makeText(this, "Registrando cita...", Toast.LENGTH_SHORT).show();

        String citaId = db.collection("citas").document().getId();
        cita.setIdCita(citaId);

        db.collection("citas")
                .document(citaId)
                .set(cita.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Cita registrada exitosamente", Toast.LENGTH_SHORT).show();
                    mostrar_ver_citas_disponibles();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firestore", "Error al registrar cita", e);
                });
    }
    private void mostrar_ver_citas_disponibles() {
        try {
            contenedorVistas.removeAllViews();
            View view = getLayoutInflater().inflate(R.layout.ver_mis_citas_cliente, contenedorVistas, false);
            contenedorVistas.addView(view);
            toolbar.setTitle("Mis Citas");

            RecyclerView rvCitas = view.findViewById(R.id.rvCitasCliente);
            LinearLayout llSinCitas = view.findViewById(R.id.llSinCitas);
            Button btnNueva = view.findViewById(R.id.btnNuevaCita);
            Button btnPrimera = view.findViewById(R.id.btnAgendarPrimeraCita);

            if (rvCitas == null || llSinCitas == null) {
                Toast.makeText(this, "Error: Vistas no encontradas en el layout", Toast.LENGTH_LONG).show();
                return;
            }

            if (btnNueva != null && btnPrimera != null) {
                View.OnClickListener irAFormulario = v -> mostrar_cita_cliente(null);
                btnNueva.setOnClickListener(irAFormulario);
                btnPrimera.setOnClickListener(irAFormulario);
            }

            rvCitas.setLayoutManager(new LinearLayoutManager(this));
            List<Cita> listaCitas = new ArrayList<>();
            CitaAdapter adapter = new CitaAdapter(this, listaCitas);
            rvCitas.setAdapter(adapter);

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            String myUid = user.getUid();

            db.collection("citas")
                    .whereEqualTo("idUsuario", myUid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e("CitasError", "Error al cargar citas", error);
                            return;
                        }

                        if (value != null) {
                            listaCitas.clear();

                            for (DocumentSnapshot doc : value.getDocuments()) {
                                try {
                                    // ✅ CAMBIA ESTO: Usa toObject en lugar de fromFirestore
                                    Cita cita = doc.toObject(Cita.class);
                                    if (cita != null) {
                                        cita.setIdCita(doc.getId()); // Asegúrate de tener este setter
                                        listaCitas.add(cita);
                                    }
                                } catch (Exception e) {
                                    Log.e("CitasError", "Error al parsear cita: " + doc.getId(), e);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (listaCitas.isEmpty()) {
                                rvCitas.setVisibility(View.GONE);
                                llSinCitas.setVisibility(View.VISIBLE);
                            } else {
                                rvCitas.setVisibility(View.VISIBLE);
                                llSinCitas.setVisibility(View.GONE);
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e("CitasError", "Error general", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void mostrarDialogoConfirmarSolicitud(Servicio servicio) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirmar Selección")
                .setMessage("¿Deseas solicitar el servicio de " + servicio.getNombreServicio() + "?")
                .setPositiveButton("SÍ, SOLICITAR", (dialog, which) -> {
                    // AQUÍ es donde llamas al cambio de vista pasando el nombre
                    mostrar_cita_cliente(servicio.getNombreServicio());
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    private void abrirFormularioCitaConServicio(String nombreServicio) {
        // Aquí llamas a tu método existente de mostrar cita
        mostrar_cita_cliente(null);

        // Nota: Para que esto funcione perfecto, tu método mostrar_cita_cliente()
        // debería recibir un String para pre-seleccionar el servicio en el Spinner de citas.
    }
    private void mostarServicioDisponible() {
        contenedorVistas.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.gestion_servicios_administrador, contenedorVistas); // O el layout que uses para listar
        toolbar.setTitle("Servicios Disponibles");

        // Ocultar el botón de "Agregar Nuevo" si usas el mismo layout del admin
        Button btnAgregar = view.findViewById(R.id.btnAgregarNuevoServicio);
        if (btnAgregar != null) btnAgregar.setVisibility(View.GONE);

        RecyclerView rvServicios = view.findViewById(R.id.rvServiciosAdmin);
        rvServicios.setLayoutManager(new LinearLayoutManager(this));

        List<Servicio> listaServicios = new ArrayList<>();

        // Configuramos el adaptador con la acción de "Solicitar"
        ServicioAdapter adapter = new ServicioAdapter(this, listaServicios, new ServicioAdapter.OnServicioClickListener() {
            @Override
            public void onServicioClick(Servicio servicio) {
                // AQUÍ LA LÓGICA PARA SOLICITAR
               mostrarDialogoConfirmarSolicitud(servicio);
                //mostrar_cita_cliente(servicio.getNombreServicio());
            }

            @Override
            public void onServicioLongClick(Servicio servicio) {
                // Opcional: mostrar detalles extendidos
            }

        });

        rvServicios.setAdapter(adapter);

        // Cargar datos desde la misma colección "servicios" que usa el Admin
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
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar servicios", Toast.LENGTH_SHORT).show());
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
                                        Toast.makeText(InicioCliente.this,
                                                "✅ Contraseña cambiada exitosamente",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        String errorMessage = "Error al cambiar contraseña";
                                        if (updateTask.getException() != null) {
                                            errorMessage = updateTask.getException().getMessage();
                                        }
                                        Toast.makeText(InicioCliente.this, errorMessage,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(InicioCliente.this,
                                "❌ Contraseña actual incorrecta",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ejecutarCambioPassword(String contraseñaActual, String nuevaContraseña) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Re-autenticación por seguridad (obligatorio en Firebase para cambiar pass)
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), contraseñaActual);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(nuevaContraseña)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "✅ Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                        mostrarVistaInicio(); // Regresa al inicio tras el éxito
                                    } else {
                                        Toast.makeText(this, "Error: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "❌ La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void configurarVistaMascotas(View view) {
        RecyclerView rvMascotas = view.findViewById(R.id.rvMascotas);
        TextView tvSinMascotas = view.findViewById(R.id.tvSinMascotas);
        Button btnAgregarMascota = view.findViewById(R.id.btnAgregarMascota);

        rvMascotas.setLayoutManager(new LinearLayoutManager(this));
        List<Mascota> listaMascotas = new ArrayList<>();

        // SOLUCIÓN: Usar un holder o variable final
        final MascotaAdapter[] adapterHolder = new MascotaAdapter[1];

        // Configurar adaptador con listener completo
        MascotaAdapter adapter = new MascotaAdapter(this, listaMascotas, false, new MascotaAdapter.OnMascotaListener() {
            @Override
            public void onEditarMascota(Mascota mascota, int position) {
                // Llamar al método de editar
                mostrarDialogoEditarMascota(mascota, position, listaMascotas, adapterHolder[0]);
            }

            @Override
            public void onEliminarMascota(Mascota mascota, int position) {
                // Llamar al método de eliminar
                mostrarDialogoConfirmarEliminar(mascota, position, listaMascotas, adapterHolder[0]);
            }

            @Override
            public void onMascotaClick(Mascota mascota, int position) {
                // Mostrar detalles de la mascota
                mostrarDetallesMascota(mascota);
            }
        });

        // Guardar el adaptador en el holder
        adapterHolder[0] = adapter;
        rvMascotas.setAdapter(adapter);

        // Cargar datos
        cargarMascotasDesdeFirebase(listaMascotas, adapter, tvSinMascotas, rvMascotas);

        btnAgregarMascota.setOnClickListener(v -> mostrar_registro_mascota());
    }

    // Método para mostrar detalles de la mascota
    private void mostrarDetallesMascota(Mascota mascota) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de " + mascota.getNombre());

        String detalles = "Especie: " + mascota.getEspecie() + "\n" +
                "Raza: " + mascota.getRaza() + "\n" +
                "Edad: " + mascota.getEdad() + " años\n" +
                "Sexo: " + mascota.getSexo() + "\n" ;

        builder.setMessage(detalles);
        builder.setPositiveButton("Cerrar", null);

        // Agregar botón de editar si es el dueño
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getUid().equals(mascota.getIdUsuario())) {
            builder.setNeutralButton("Editar", (dialog, which) -> {
                // Encontrar la posición de la mascota
                // (Necesitarías buscar en tu lista)
            });
        }

        builder.show();
    }

    // Método para confirmar eliminación
    private void mostrarDialogoConfirmarEliminar(Mascota mascota, int position,
                                                 List<Mascota> listaMascotas,
                                                 MascotaAdapter adapter) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Mascota")
                .setMessage("¿Estás seguro de eliminar a " + mascota.getNombre() + "?\nEsta acción no se puede deshacer.")
                .setPositiveButton("ELIMINAR", (dialog, which) -> {
                    eliminarMascotaDeFirestore(mascota, position, listaMascotas, adapter);
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    // Método para eliminar de Firestore
    private void eliminarMascotaDeFirestore(Mascota mascota, int position,
                                            List<Mascota> listaMascotas,
                                            MascotaAdapter adapter) {
        FirebaseUser user = mAuth.getCurrentUser();

        // Verificar permisos (solo el dueño puede eliminar)
        if (user == null || !user.getUid().equals(mascota.getIdUsuario())) {
            Toast.makeText(this, "No tienes permisos para eliminar esta mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("mascotas")
                .document(mascota.getIdMascota())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Mascota eliminada exitosamente", Toast.LENGTH_SHORT).show();

                    // Eliminar de la lista local
                    listaMascotas.remove(position);
                    adapter.notifyItemRemoved(position);

                    // Verificar si quedan mascotas
                    if (listaMascotas.isEmpty()) {
                        RecyclerView rvMascotas = findViewById(R.id.rvMascotas);
                        TextView tvSinMascotas = findViewById(R.id.tvSinMascotas);
                        if (rvMascotas != null && tvSinMascotas != null) {
                            rvMascotas.setVisibility(View.GONE);
                            tvSinMascotas.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Método para actualizar en Firestore (ya lo tienes, pero lo dejo completo)
    private void actualizarMascotaEnFirestore(Mascota mascota, int position,
                                              List<Mascota> listaMascotas,
                                              MascotaAdapter adapter,
                                              Dialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();

        // Verificar permisos
        if (user == null || !user.getUid().equals(mascota.getIdUsuario())) {
            Toast.makeText(this, "No tienes permisos para editar esta mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("mascotas")
                .document(mascota.getIdMascota())
                .set(mascota.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Mascota actualizada exitosamente", Toast.LENGTH_SHORT).show();

                    // Actualizar lista local
                    listaMascotas.set(position, mascota);
                    adapter.notifyItemChanged(position);

                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarAutenticacion();
    }
}