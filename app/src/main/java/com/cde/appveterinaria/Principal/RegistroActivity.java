// Si quieres mantener el archivo pero deshabilitarlo, comenta todo y agrega:
package com.cde.appveterinaria.Principal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cde.appveterinaria.R;

public class RegistroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Cerrar esta actividad y redirigir a MainActivity
        finish();
    }
}