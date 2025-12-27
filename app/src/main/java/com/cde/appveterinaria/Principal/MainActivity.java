package com.cde.appveterinaria.Principal;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.cde.appveterinaria.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private MaterialToolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupDrawerLayout();
        setupNavigationView();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar3);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Veterinaria App");
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

            if (id == R.id.nav_login) {
                mostrarDialogLogin();
            } else if (id == R.id.nav_registro) {
                mostrarDialogRegistro();
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

    // ========== DIALOGO DE LOGIN ==========
    private void mostrarDialogLogin() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_login);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        EditText txtUsuario = dialog.findViewById(R.id.txtUsuario);
        EditText txtPassword = dialog.findViewById(R.id.txtPassword);
        Button btnLogin = dialog.findViewById(R.id.btnLogin);
        TextView tvRegistrar = dialog.findViewById(R.id.tvRegistrar);

        btnLogin.setOnClickListener(v -> {
            String usuario = txtUsuario.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();

            if (validarCamposLogin(usuario, password)) {
                iniciarSesionFirebase(usuario, password, dialog);
            }
        });

        tvRegistrar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogRegistro();
        });

        dialog.show();
    }

    private boolean validarCamposLogin(String usuario, String password) {
        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void iniciarSesionFirebase(String email, String password, Dialog dialog) {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(MainActivity.this,
                                    "¡Bienvenido " + user.getEmail() + "!",
                                    Toast.LENGTH_SHORT).show();

                            dialog.dismiss();

                            // Redirigir según el email
                            if (email.equals("admin@gmail.com")) {
                                // Admin especial
                                Intent intent = new Intent(MainActivity.this,
                                        com.cde.appveterinaria.FragmentoAdministrador.InicioAdministrador.class);
                                startActivity(intent);
                            } else {
                                // Usuario normal
                                Intent intent = new Intent(MainActivity.this,
                                        com.cde.appveterinaria.FragmentoCliente.InicioCliente.class);
                                startActivity(intent);
                            }
                            finish();
                        }
                    } else {
                        manejarErrorLogin(task.getException());
                    }
                });
    }

    private void manejarErrorLogin(Exception exception) {
        String errorMessage = "Error al iniciar sesión";

        if (exception != null) {
            String error = exception.getMessage();
            if (error.contains("wrong-password")) {
                errorMessage = "Contraseña incorrecta";
            } else if (error.contains("user-not-found")) {
                errorMessage = "Usuario no encontrado. Regístrate primero.";
            } else if (error.contains("too-many-requests")) {
                errorMessage = "Demasiados intentos. Intenta más tarde";
            } else {
                errorMessage = "Error: " + error;
            }
        }

        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    // ========== DIALOGO DE REGISTRO ==========
    private void mostrarDialogRegistro() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_registro);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        EditText txtNombre = dialog.findViewById(R.id.txtNombre);
        EditText txtCorreo = dialog.findViewById(R.id.txtCorreoRegistro);
        EditText txtPassword = dialog.findViewById(R.id.txtPasswordRegistro);
        EditText txtConfirmPassword = dialog.findViewById(R.id.txtConfirmarPasswordRegistro);
        Button btnRegistrar = dialog.findViewById(R.id.btnRegistrar);
        TextView tvVolverLogin = dialog.findViewById(R.id.tvVolverLogin);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString().trim();
            String correo = txtCorreo.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            String confirmPassword = txtConfirmPassword.getText().toString().trim();

            if (validarCamposRegistro(nombre, correo, password, confirmPassword)) {
                registrarUsuarioFirebase(nombre, correo, password, dialog);
            }
        });

        // Agregar funcionalidad al enlace "Volver a Login"
        tvVolverLogin.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogLogin();
        });

        dialog.show();
    }

    private boolean validarCamposRegistro(String nombre, String correo, String password, String confirmacion) {
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmacion)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void manejarErrorRegistro(Exception exception) {
        String errorMessage = "Error al crear la cuenta";

        if (exception != null) {
            String error = exception.getMessage();
            if (error.contains("email-already-in-use")) {
                errorMessage = "Este correo ya está registrado";
            } else if (error.contains("weak-password")) {
                errorMessage = "La contraseña es demasiado débil";
            } else {
                errorMessage = "Error: " + error;
            }
        }

        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    // Método para crear usuario en Firestore
    // Método para crear usuario en Firestore
    private void crearUsuarioEnFirestore(String uid, String nombre, String correo, String rol) {
        Log.d("DEBUG_FIRESTORE", "=== INICIANDO CREACIÓN EN FIRESTORE ===");
        Log.d("DEBUG_FIRESTORE", "UID: " + uid);
        Log.d("DEBUG_FIRESTORE", "Nombre: " + nombre);
        Log.d("DEBUG_FIRESTORE", "Email: " + correo);
        Log.d("DEBUG_FIRESTORE", "Rol: " + rol);

        if (uid == null || uid.isEmpty()) {
            Log.e("DEBUG_FIRESTORE", "❌ ERROR: UID es null o vacío");
            Toast.makeText(MainActivity.this, "Error: UID no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", uid);
        usuario.put("nombre", nombre);
        usuario.put("email", correo);
        usuario.put("rol", rol);
        usuario.put("fechaRegistro", new Date());
        usuario.put("fechaRegistroFormateada", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
        usuario.put("emailVerificado", false);
        usuario.put("activo", true);

        // Usa la instancia 'db' que ya tienes como variable de clase
        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DEBUG_FIRESTORE", "✅ ÉXITO: Documento creado en Firestore");
                        Log.d("DEBUG_FIRESTORE", "ID Documento: " + uid);
                        Log.d("DEBUG_FIRESTORE", "Colección: usuarios");

                        // Verifica que realmente se creó
                        db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            Log.d("DEBUG_FIRESTORE", "✅ CONFIRMADO: Documento existe");
                                            Log.d("DEBUG_FIRESTORE", "Datos: " + documentSnapshot.getData());
                                        } else {
                                            Log.e("DEBUG_FIRESTORE", "❌ El documento NO existe después de set()");
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("DEBUG_FIRESTORE", "Error al verificar documento: " + e.getMessage());
                                    }
                                });

                        Toast.makeText(MainActivity.this, "✅ Perfil guardado en Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DEBUG_FIRESTORE", "❌ ERROR al crear en Firestore: " + e.getMessage());
                        Log.e("DEBUG_FIRESTORE", "Stack Trace: ", e);

                        Toast.makeText(MainActivity.this,
                                "⚠️ Error al guardar perfil: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    // En onCreate() o en un método de prueba
    private void verificarUsuariosEnFirestore() {
        FirebaseFirestore.getInstance().collection("usuarios")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d("DEBUG_FIRESTORE", "Total documentos en 'usuarios': " + queryDocumentSnapshots.size());
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d("DEBUG_FIRESTORE", "Documento ID: " + doc.getId());
                            Log.d("DEBUG_FIRESTORE", "Datos: " + doc.getData());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DEBUG_FIRESTORE", "Error al leer usuarios: " + e.getMessage());
                    }
                });
    }
    private void verificarUsuarioActual() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("DEBUG_AUTH", "=== USUARIO ACTUAL EN AUTH ===");
            Log.d("DEBUG_AUTH", "UID: " + currentUser.getUid());
            Log.d("DEBUG_AUTH", "Email: " + currentUser.getEmail());
            Log.d("DEBUG_AUTH", "Nombre: " + currentUser.getDisplayName());
            Log.d("DEBUG_AUTH", "Email verificado: " + currentUser.isEmailVerified());
        } else {
            Log.d("DEBUG_AUTH", "❌ No hay usuario autenticado");
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si ya hay usuario logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya está logueado, redirigir según rol
            String email = currentUser.getEmail();
            if (email != null && email.equals("admin@gmail.com")) {
                startActivity(new Intent(this,
                        com.cde.appveterinaria.FragmentoAdministrador.InicioAdministrador.class));
            } else {
                startActivity(new Intent(this,
                        com.cde.appveterinaria.FragmentoCliente.InicioCliente.class));
            }
            finish();
        }
    }
    private void registrarUsuarioFirebase(String nombre, String correo, String password, Dialog dialog) {
        Toast.makeText(this, "Creando cuenta...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // VERIFICAR UID INMEDIATAMENTE
                            Log.d("DEBUG_AUTH", "✅ Usuario creado en Auth");
                            Log.d("DEBUG_AUTH", "UID obtenido: " + uid);
                            Log.d("DEBUG_AUTH", "Email: " + user.getEmail());
                            Log.d("DEBUG_AUTH", "¿Email verificado?: " + user.isEmailVerified());

                            // Actualizar nombre del perfil PRIMERO
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d("DEBUG_AUTH", "✅ Nombre actualizado en perfil de Auth");

                                            // Ahora crear en Firestore DESPUÉS de actualizar el perfil
                                            crearUsuarioEnFirestore(uid, nombre, correo, "cliente");

                                            // Enviar email de verificación
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(emailTask -> {
                                                        if (emailTask.isSuccessful()) {
                                                            Log.d("DEBUG_AUTH", "✅ Email de verificación enviado");
                                                            Toast.makeText(MainActivity.this,
                                                                    "Se ha enviado un email de verificación a " + correo,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                            // ESPERAR ANTES DE CERRAR SESIÓN
                                            new android.os.Handler().postDelayed(() -> {
                                                Log.d("DEBUG_AUTH", "Cerrando sesión después de 3 segundos...");
                                                mAuth.signOut();

                                                Toast.makeText(MainActivity.this,
                                                        "✅ ¡Cuenta creada exitosamente!\n" +
                                                                "Verifica tu email e inicia sesión",
                                                        Toast.LENGTH_LONG).show();

                                                dialog.dismiss();

                                                // VERIFICAR QUE SE CREÓ EN FIRESTORE
                                                new android.os.Handler().postDelayed(() -> {
                                                    verificarUsuariosEnFirestore();
                                                }, 1000);

                                            }, 3000); // Esperar 3 segundos

                                        } else {
                                            Log.e("DEBUG_AUTH", "❌ Error al actualizar perfil", profileTask.getException());
                                            Toast.makeText(MainActivity.this,
                                                    "Error al actualizar perfil",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Log.e("DEBUG_AUTH", "❌ Usuario es null después de crear");
                            Toast.makeText(MainActivity.this,
                                    "Error: Usuario no creado",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        manejarErrorRegistro(task.getException());
                    }
                });
    }
    private void pruebaFirestoreSimple() {
        String testId = "test_" + System.currentTimeMillis();
        Map<String, Object> testData = new HashMap<>();
        testData.put("test", true);
        testData.put("timestamp", new Date());
        testData.put("mensaje", "Prueba de Firestore desde Android");

        Log.d("TEST_FIRESTORE", "=== INICIANDO PRUEBA DE FIRESTORE ===");
        Log.d("TEST_FIRESTORE", "ID Test: " + testId);

        db.collection("usuarios")
                .document(testId)
                .set(testData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TEST_FIRESTORE", "✅ Prueba exitosa - Firestore funciona");
                            Toast.makeText(MainActivity.this, "Firestore: OK", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("TEST_FIRESTORE", "❌ Prueba fallida: " + task.getException().getMessage());
                            Toast.makeText(MainActivity.this,
                                    "Firestore ERROR: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }
}