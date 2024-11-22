package com.example.marketplace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PerfilFragment extends Fragment {

    DataHelper dh;
    SQLiteDatabase bd;
    Usuario usuario;
    TextView perfilNombre, perfilCorreo, perfilCantidad, perfilHoraInicio, perfilHoraTermino;
    EditText perfilValueHoraInicio, perfilValueHoraTermino;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        usuario = Usuario.getInstance();

        dh = new DataHelper(requireContext(), "app.db", null, 3);
        bd = dh.getWritableDatabase();

        perfilNombre = view.findViewById(R.id.perfilNombre);
        perfilCorreo = view.findViewById(R.id.perfilCorreo);
        perfilCantidad = view.findViewById(R.id.perfilCantidad);
        perfilHoraInicio = view.findViewById(R.id.perfilHoraInicio);
        perfilHoraTermino = view.findViewById(R.id.perfilHoraTermino);

        perfilValueHoraInicio = view.findViewById(R.id.perfilValueHoraInicio);
        perfilValueHoraTermino = view.findViewById(R.id.perfilValueHoraTermino);

        obtenerDatosUsuario();

        Button botonEliminarCuenta = view.findViewById(R.id.botonEliminarCuenta);
        Button perfilBotonGuardar = view.findViewById(R.id.perfilBotonGuardar);

        botonEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarUsuarioYProductos(usuario);
            }
        });

        perfilBotonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String horaInicio = perfilValueHoraInicio.getText().toString();
                String horaTermino = perfilValueHoraTermino.getText().toString();

                if (horaInicio.isEmpty()){
                    Toast.makeText(requireContext(), "Debe ingresar un valor de inicio", Toast.LENGTH_SHORT).show();
                }

                if (horaTermino.isEmpty()){
                    Toast.makeText(requireContext(), "Debe ingresar un valor de término", Toast.LENGTH_SHORT).show();
                }

                int usuarioId = usuario.getId();

                actualizarHorarioUsuario(usuarioId, horaInicio, horaTermino);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        obtenerDatosUsuario();
    }

    private int obtenerCantidadDeProductos(int idUsuario){
        int cantidad = 0;
        String query = "SELECT COUNT(*) FROM producto WHERE id_usuario = ?";

        Cursor cursor = bd.rawQuery(query, new String[]{String.valueOf(idUsuario)});
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0);
        }
        cursor.close();
        return cantidad;
    }

    private void eliminarUsuarioYProductos(Usuario usuario) {
        int userId = usuario.getId();

        try {
            String deleteProductosQuery = "DELETE FROM producto WHERE id_usuario = ?";
            bd.execSQL(deleteProductosQuery, new Object[]{userId});

            String deleteUsuarioQuery = "DELETE FROM usuario WHERE id = ?";
            bd.execSQL(deleteUsuarioQuery, new Object[]{userId});

            Usuario.cerrarSesion();

            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(requireContext(), "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al eliminar la cuenta.", Toast.LENGTH_SHORT).show();
        }

    }

    private void obtenerDatosUsuario(){
        perfilNombre.setText("Usuario: " + usuario.getNombre());
        perfilCorreo.setText("Correo: " + usuario.getCorreo());
        perfilCantidad.setText("Cantidad de productos: " + obtenerCantidadDeProductos(usuario.getId()));

        System.out.println("HORA DE INICIO: " + usuario.getHoraInicio());
        System.out.println("HORA DE FIN: " + usuario.getHoraFin());

        perfilValueHoraInicio.setText(usuario.getHoraInicio());
        perfilValueHoraTermino.setText(usuario.getHoraFin());
    }

    private void actualizarHorarioUsuario(int usuarioId, String horaInicio, String horaTermino) {
        ContentValues reg = new ContentValues();
        reg.put("hora_inicio", horaInicio);
        reg.put("hora_fin", horaTermino);

        System.out.println("HORA ENVIADA A ACTUALIZAR: " + horaInicio + horaTermino);

        int rowsUpdated = bd.update("usuario", reg, "id = ?", new String[]{String.valueOf(usuarioId)});

        if (rowsUpdated > 0) {
            Toast.makeText(requireContext(), "Horario actualizado correctamente", Toast.LENGTH_SHORT).show();
            usuario.setHoraInicio(horaInicio);
            usuario.setHoraFin(horaTermino);
        } else {
            Toast.makeText(requireContext(), "No se pudo actualizar el horario", Toast.LENGTH_SHORT).show();
        }
    }
}