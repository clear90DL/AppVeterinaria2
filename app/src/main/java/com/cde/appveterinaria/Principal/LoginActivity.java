package com.cde.appveterinaria.Principal;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cde.appveterinaria.FragmentoAdministrador.InicioAdministrador;
import com.cde.appveterinaria.FragmentoCliente.InicioCliente;
import com.cde.appveterinaria.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText txtUsuario, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias a las vistas
        txtUsuario = findViewById(R.id.txtUsuario);
        txtPassword = findViewById(R.id.txtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegistrar = findViewById(R.id.tvRegistrar);

        // Verificar si ya hay un usuario logueado
        checkCurrentUser();

        // Botón de login
        btnLogin.setOnClickListener(v -> {
            iniciarSesion();
        });

        // Enlace a registro
        tvRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuario ya está logueado, redirigir según rol
            String email = currentUser.getEmail();
            if (email != null && email.equals("admin@gmail.com")) {
                // Redirigir directamente a administrador
                Intent intent = new Intent(LoginActivity.this, InicioAdministrador.class);
                startActivity(intent);
                finish();
            } else {
                // Redirigir a cliente
                Intent intent = new Intent(LoginActivity.this, InicioCliente.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void iniciarSesion() {
        String correo = txtUsuario.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // Validaciones básicas
        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progreso
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        // Verificar si es el admin especial
        if (correo.equals("admin@gmail.com") && password.equals("123456")) {
            // Login especial para admin (sin Firebase)
            loginAdminDirecto(correo);
            return;
        }

        // Para todos los demás usuarios, usar Firebase
        loginConFirebase(correo, password);
    }

    private void loginAdminDirecto(String correo) {
        Toast.makeText(LoginActivity.this,
                "¡Bienvenido Administrador!",
                Toast.LENGTH_SHORT).show();

        // Verificar si el admin existe en Firestore, si no, crearlo
        verificarOCrearAdminEnFirestore(correo);

        // Redirigir al panel administrador
        Intent intent = new Intent(LoginActivity.this, InicioAdministrador.class);
        startActivity(intent);
        finish();
    }

    private void verificarOCrearAdminEnFirestore(String correo) {
        // Verificar si el admin ya existe en Firestore
        db.collection("usuarios")
                .whereEqualTo("email", correo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Crear admin en Firestore si no existe
                            crearAdminEnFirestore(correo);
                        }
                    }
                });
    }

    private void crearAdminEnFirestore(String correo) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("email", correo);
        usuario.put("rol", "admin");
        usuario.put("nombre", "Administrador Principal");
        usuario.put("fechaRegistro", System.currentTimeMillis());
        usuario.put("esAdminEspecial", true);

        // Usar el email como ID del documento
        db.collection("usuarios")
                .document(correo.replace(".", "_")) // Reemplazar puntos por guiones bajos para ID válido
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Admin especial creado en Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al crear admin especial", e);
                });
    }

    private void loginConFirebase(String correo, String password) {
        // Autenticar con Firebase para usuarios normales
        mAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this,
                                    "¡Bienvenido " + user.getEmail() + "!",
                                    Toast.LENGTH_SHORT).show();

                            // Verificar si el email está verificado
                            if (!user.isEmailVerified()) {
                                mostrarDialogVerificacionEmail();
                            } else {
                                // Determinar rol del usuario
                                determinarRolUsuario(user.getEmail());
                            }
                        }
                    } else {
                        // Error en login
                        manejarErrorLogin(task.getException());
                    }
                });
    }

    private void manejarErrorLogin(Exception exception) {
        String errorMessage = "Error al iniciar sesión";

        if (exception != null) {
            String error = exception.getMessage();
            if (error.contains("invalid-email")) {
                errorMessage = "Correo electrónico inválido";
            } else if (error.contains("user-not-found")) {
                errorMessage = "Usuario no encontrado. Regístrate primero.";
            } else if (error.contains("wrong-password")) {
                errorMessage = "Contraseña incorrecta";
            } else if (error.contains("too-many-requests")) {
                errorMessage = "Demasiados intentos. Intenta más tarde";
            } else if (error.contains("network-error") || error.contains("timeout")) {
                errorMessage = "Error de conexión. Verifica tu internet";
            } else if (error.contains("user-disabled")) {
                errorMessage = "Esta cuenta ha sido deshabilitada";
            } else {
                errorMessage = "Error: " + error;
            }
        }

        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void determinarRolUsuario(String email) {
        // Para el admin especial, redirigir directamente
        if (email.equals("admin@gmail.com")) {
            Intent intent = new Intent(LoginActivity.this, InicioAdministrador.class);
            startActivity(intent);
            finish();
            return;
        }

        // Para otros usuarios, verificar en Firestore
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String rol = document.getString("rol");

                        if ("admin".equals(rol)) {
                            // Redirigir a panel de administrador
                            Intent intent = new Intent(LoginActivity.this, InicioAdministrador.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Redirigir a panel de cliente
                            Intent intent = new Intent(LoginActivity.this, InicioCliente.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Si no existe en Firestore, crear registro por defecto como cliente
                        crearUsuarioEnFirestore(email, "cliente");
                        Intent intent = new Intent(LoginActivity.this, InicioCliente.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void crearUsuarioEnFirestore(String email, String rol) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("email", email);
            usuario.put("rol", rol);
            usuario.put("nombre", user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
            usuario.put("fechaRegistro", System.currentTimeMillis());

            db.collection("usuarios")
                    .document(user.getUid())
                    .set(usuario)
                    .addOnSuccessListener(aVoid -> {
                        // Usuario creado en Firestore
                    })
                    .addOnFailureListener(e -> {
                        // Error al crear en Firestore
                    });
        }
    }

    private void mostrarDialogVerificacionEmail() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verificacion_email);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        Button btnReenviar = dialog.findViewById(R.id.btnReenviarVerificacion);
        Button btnContinuar = dialog.findViewById(R.id.btnContinuarSinVerificar);
        Button btnCerrarSesion = dialog.findViewById(R.id.btnCerrarSesionVerificar);

        btnReenviar.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Email de verificación enviado a " + user.getEmail(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            // Determinar rol aunque el email no esté verificado
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                determinarRolUsuario(user.getEmail());
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut();
            dialog.dismiss();
            Toast.makeText(LoginActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // En LoginActivity, si lo tienes como actividad separada:
    @Override
    protected void onStart() {
        super.onStart();

        // Verificar si ya hay usuario logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya está logueado, redirigir según rol
            if (currentUser.getEmail().equals("admin@gmail.com")) {
                startActivity(new Intent(this, InicioAdministrador.class));
            } else {
                startActivity(new Intent(this, InicioCliente.class));
            }
            finish();
        }
    }
}