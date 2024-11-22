package com.example.marketplace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    DataHelper dh;
    SQLiteDatabase bd;

    EditText registroNombre, registroEmail, registroContra;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dh = new DataHelper(this, "app.db", null, 3);
        bd = dh.getWritableDatabase();

        registroNombre = findViewById(R.id.registroNombre);
        registroEmail = findViewById(R.id.registroEmail);
        registroContra = findViewById(R.id.registroContra);

        Button registroBoton = findViewById(R.id.registroBoton);

        Button volverLoginBoton = findViewById(R.id.volverLoginBoton);

        registroBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nuevoNombre = registroNombre.getText().toString();
                String nuevoCorreo = registroEmail.getText().toString();
                String nuevaContra = registroContra.getText().toString();

                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Por favor, ingrese su nombre de usuario", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (nuevoCorreo.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Por favor, ingresa un correo.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (nuevaContra.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Por favor, ingresa la contraseña.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor cursor = bd.rawQuery("SELECT * FROM usuario WHERE email = ?", new String[]{nuevoCorreo});
                if (cursor.getCount() > 0) {
                    Toast.makeText(RegistroActivity.this, "Este correo ya está en uso. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }
                cursor.close();


                ContentValues reg= new ContentValues();
                reg.put("nombre", registroNombre.getText().toString());
                reg.put("email", registroEmail.getText().toString());
                reg.put("contra", registroContra.getText().toString());
                reg.put("hora_inicio", (String) null);
                reg.put("hora_fin", (String) null);

                long resp = bd.insert("usuario", null, reg);
                if(resp==-1){
                    Toast.makeText(RegistroActivity.this,"No se pudo crear el usuario", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegistroActivity.this,"Usuario creado", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });

        volverLoginBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
