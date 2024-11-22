package com.example.marketplace;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MisProductosFragment extends Fragment {
    TableLayout tablaProductos;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    DataHelper dh;
    SQLiteDatabase bd;

    Button botonEliminar, botonModificar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_productos, container, false);
        dh = new DataHelper(requireContext(), "app.db", null, 3);
        bd = dh.getWritableDatabase();

        tablaProductos = view.findViewById(R.id.tablaProductos);
        cargarLista();

        botonEliminar = view.findViewById(R.id.botonEliminar);
        botonModificar = view.findViewById(R.id.botonModificar);

        botonEliminar.setOnClickListener(v -> {
            List<CheckBox> checkBoxesParaEliminar = new ArrayList<>();

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    checkBoxesParaEliminar.add(checkBox);
                }
            }

            for (CheckBox checkBox : checkBoxesParaEliminar) {
                int productoId = (int) checkBox.getTag();
                eliminarProducto(productoId);
            }

            cargarLista();
        });

        botonModificar.setOnClickListener(v -> {
            CheckBox selectedCheckBox = null;

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    selectedCheckBox = checkBox;
                    break;
                }
            }

            if (selectedCheckBox == null) {
                Toast.makeText(requireContext(), "Selecciona un producto para modificar.", Toast.LENGTH_SHORT).show();
                return;
            }

            int productoId = (int) selectedCheckBox.getTag();

            String query = "SELECT nombre, precio, cantidad, latitud, longitud FROM producto WHERE id = ?";
            Cursor cursor = bd.rawQuery(query, new String[]{String.valueOf(productoId)});

            if (!cursor.moveToFirst()) {
                Toast.makeText(requireContext(), "Error al obtener datos del producto.", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            String nombreActual = cursor.getString(0);
            int precioActual = cursor.getInt(1);
            int cantidadActual = cursor.getInt(2);
            double latitudActual = cursor.getDouble(3);
            double longitudActual = cursor.getDouble(4);
            cursor.close();

            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Modificar Producto");

                // Layout del diálogo
                LinearLayout layout = new LinearLayout(requireContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                // Campo para el nombre
                final EditText inputNombre = new EditText(requireContext());
                inputNombre.setHint("Nombre");
                inputNombre.setText(nombreActual);
                layout.addView(inputNombre);

                // Campo para el precio
                final EditText inputPrecio = new EditText(requireContext());
                inputPrecio.setHint("Precio");
                inputPrecio.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputPrecio.setText(String.valueOf(precioActual));
                layout.addView(inputPrecio);

                // Campo para la cantidad
                final EditText inputCantidad = new EditText(requireContext());
                inputCantidad.setHint("Cantidad");
                inputCantidad.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputCantidad.setText(String.valueOf(cantidadActual));
                layout.addView(inputCantidad);

                // Campo para la latitud
                final EditText inputLatitud = new EditText(requireContext());
                inputLatitud.setHint("Latitud");
                inputLatitud.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                inputLatitud.setText(String.valueOf(latitudActual));
                layout.addView(inputLatitud);

                // Campo para la longitud
                final EditText inputLongitud = new EditText(requireContext());
                inputLongitud.setHint("Longitud");
                inputLongitud.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                inputLongitud.setText(String.valueOf(longitudActual));
                layout.addView(inputLongitud);

                builder.setView(layout);

                // Botón Guardar
                builder.setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = inputNombre.getText().toString().trim();
                    String nuevoPrecio = inputPrecio.getText().toString().trim();
                    String nuevaCantidad = inputCantidad.getText().toString().trim();
                    String nuevaLatitud = inputLatitud.getText().toString().trim();
                    String nuevaLongitud = inputLongitud.getText().toString().trim();

                    // Validar entrada
                    if (nuevoNombre.isEmpty() || nuevoPrecio.isEmpty() || nuevaCantidad.isEmpty() || nuevaLatitud.isEmpty() || nuevaLongitud.isEmpty()) {
                        Toast.makeText(requireContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Actualizar la base de datos
                    ContentValues valores = new ContentValues();
                    valores.put("nombre", nuevoNombre);
                    valores.put("precio", Integer.parseInt(nuevoPrecio));
                    valores.put("cantidad", Integer.parseInt(nuevaCantidad));
                    valores.put("latitud", Double.parseDouble(nuevaLatitud));
                    valores.put("longitud", Double.parseDouble(nuevaLongitud));

                    int filasActualizadas = bd.update("producto", valores, "id = ?", new String[]{String.valueOf(productoId)});
                    if (filasActualizadas > 0) {
                        Toast.makeText(requireContext(), "Producto modificado exitosamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Error al modificar el producto.", Toast.LENGTH_SHORT).show();
                    }

                    cargarLista();
                });

                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

                builder.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error al mostrar el diálogo.", Toast.LENGTH_SHORT).show();
            }

        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista(){
        checkBoxes.clear();
        List<Producto> listaProductos = obtenerProductosPorIdUsuario();
        Log.d("CargarLista", "Cantidad de productos: " + listaProductos.size());

        for (int i = tablaProductos.getChildCount() - 1; i > 0; i--) {
            tablaProductos.removeViewAt(i);
        }

        float density = getResources().getDisplayMetrics().density;
        int anchoDp = 42;
        int altoDp = 42;

        int anchoPx = (int) (anchoDp * density);
        int altoPx = (int) (altoDp * density);

        for (Producto producto : listaProductos){
            TableRow fila = new TableRow(requireContext());
            CheckBox checkBox = new CheckBox(requireContext());
            TableRow.LayoutParams layoutParamsCheckBox = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            layoutParamsCheckBox.gravity = Gravity.CENTER;
            checkBox.setLayoutParams(layoutParamsCheckBox);
            checkBox.setTag(producto.id);
            checkBoxes.add(checkBox);
            fila.addView(checkBox);

            TextView nombre = new TextView(requireContext());
            nombre.setText(producto.nombre);
            nombre.setPadding(10, 10, 10, 10);
            nombre.setTextSize(18);
            nombre.setTextColor(Color.WHITE);
            fila.addView(nombre);

            TextView precio = new TextView(requireContext());
            precio.setText(String.valueOf(producto.precio));
            precio.setPadding(10, 10, 10, 10);
            precio.setTextSize(18);
            precio.setTextColor(Color.WHITE);
            precio.setTextColor(Color.parseColor("#00FF00"));
            fila.addView(precio);

            TextView cantidad = new TextView(requireContext());
            cantidad.setText(String.valueOf(producto.cantidad));
            cantidad.setPadding(10, 10, 10, 10);
            cantidad.setTextSize(18);
            cantidad.setTextColor(Color.WHITE);
            cantidad.setGravity(Gravity.CENTER);
            fila.addView(cantidad);

            ImageView imagen = new ImageView(requireContext());
            imagen.setImageResource(R.drawable.box);
            TableRow.LayoutParams layoutParamsImage = new TableRow.LayoutParams(anchoPx, altoPx);
            imagen.setLayoutParams(layoutParamsImage);
            fila.addView(imagen);

            TextView estado = new TextView(requireContext());
            estado.setText(producto.estado);
            estado.setPadding(10, 10, 10, 10);
            estado.setTextSize(18);
            estado.setTextColor(Color.parseColor("#00FF00"));
            fila.addView(estado);


            tablaProductos.addView(fila);
        }
    }

    private List<Producto> obtenerProductosPorIdUsuario() {
        Usuario usuario = Usuario.getInstance();
        int uId = usuario.getId();
        String query = "SELECT id, nombre, precio, cantidad, latitud, longitud, id_usuario FROM producto WHERE id_usuario = ?";
        Cursor c = bd.rawQuery(query, new String[]{String.valueOf(uId)});

        List<Producto> listaProductos = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(0);
                String nombre = c.getString(1);
                int precio = c.getInt(2);
                int cantidad = c.getInt(3);
                double latitud = c.getDouble(4);
                double longitud = c.getDouble(5);
                int idUsuario = c.getInt(6);

                listaProductos.add(new Producto(id, nombre, precio, cantidad, latitud, longitud, idUsuario, "Disponible"));
            } while (c.moveToNext());
        }
        c.close();
        return listaProductos;
    }

    private void eliminarProducto(int id){
        long respuesta = bd.delete("producto", "id=" + id, null);
        if (respuesta == -1){
            Toast.makeText(requireContext(),"Dato No Eliminado", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(requireContext(),"Dato Eliminado", Toast.LENGTH_LONG).show();
        }
        cargarLista();
    }
}