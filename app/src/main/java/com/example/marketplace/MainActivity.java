package com.example.marketplace;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    EditText loginCorreo, loginContra;
    DataHelper dh;
    SQLiteDatabase bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dh = new DataHelper(this, "app.db", null, 3);
        bd = dh.getWritableDatabase();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginCorreo = findViewById(R.id.loginCorreo);
        loginContra = findViewById(R.id.loginContra);

        Button loginBoton = findViewById(R.id.loginBoton);
        Button registerBoton = findViewById(R.id.linkRegistro);

        registerBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        loginBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = loginCorreo.getText().toString();
                String contra = loginContra.getText().toString();

                if (correo.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese su nombre de usuario", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (contra.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingresa un correo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (verificarLogin(correo, contra)) {
                    Intent intent = new Intent(MainActivity.this, AplicationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean verificarLogin(String correo, String contra) {
        Cursor c = bd.rawQuery("SELECT id, nombre, email, contra, hora_inicio, hora_fin FROM usuario", null);
        if (c.moveToFirst()) {
            do {
                String correoBd = c.getString(2);  // Correo de la base de datos
                String contraBd = c.getString(3);  // Contraseña de la base de datos

                if (correoBd.equals(correo) && contraBd.equals(contra)) {
                    int id = c.getInt(0);
                    String nombre = c.getString(1);
                    String horaInicio = c.getString(4);
                    String horaFin = c.getString(5);

                    Usuario userSession = Usuario.getInstance();
                    userSession.setId(id);
                    userSession.setNombre(nombre);
                    userSession.setCorreo(correoBd);
                    userSession.setHoraInicio(horaInicio);
                    userSession.setHoraFin(horaFin);

                    return true;
                }
            } while (c.moveToNext());
        }
        return false;
    }
}