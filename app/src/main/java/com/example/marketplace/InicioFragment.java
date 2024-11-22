package com.example.marketplace;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {
    DataHelper dh;
    SQLiteDatabase bd;

    Usuario usuario;
    MapView mapView;
    private ViewPager2 viewPager;
    VideoView video;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);
        usuario = Usuario.getInstance();

        dh = new DataHelper(requireContext(), "app.db", null, 3);
        bd = dh.getWritableDatabase();

        TextView textoInicio = view.findViewById(R.id.inicioNombreUsuario);
        textoInicio.setText("Bienvenido " + usuario.getNombre());

        video = view.findViewById(R.id.videoInicio);
        String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.video;
        Uri uri = Uri.parse(videoPath);
        video.setVideoURI(uri);

        MediaController mediaController = new MediaController(requireContext());
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
        video.start();

        mapView = view.findViewById(R.id.inicioMapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        double ipSantoTomasLatitud = -33.4493141;
        double ipSantoTomasLongitud = -70.6624069;

        GeoPoint IpSantoTomasPoint = new GeoPoint(ipSantoTomasLatitud, ipSantoTomasLongitud);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(IpSantoTomasPoint);

        viewPager = requireActivity().findViewById(R.id.viewPager);
        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    viewPager.setUserInputEnabled(false);
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    viewPager.setUserInputEnabled(true);
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
        cargarMarcadores();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMarcadores();
    }

    private void cargarMarcadores() {
        mapView.getOverlays().clear();
        List<Producto> productos = obtenerProductos();

        for (Producto producto : productos) {
            double lat = producto.getLatitud();
            double lon = producto.getLongitud();
            String nombre = producto.getNombre();

            GeoPoint punto = new GeoPoint(lat, lon);

            // Crear el marcador
            Marker marcador = new Marker(mapView);
            marcador.setPosition(punto);
            marcador.setTitle(nombre);
            marcador.setSnippet("Precio: " + producto.getPrecio() + " Estado: " + producto.getEstado() + "Hora de venta: inicio - " + usuario.getHoraInicio() + " término - " + usuario.getHoraFin());

            mapView.getOverlays().add(marcador);
        }
        mapView.invalidate();
    }

    public List<Producto> obtenerProductos() {
        List<Producto> listaProductos = new ArrayList<>();
        Cursor cursor = bd.rawQuery("SELECT id, nombre, precio, cantidad, latitud, longitud, id_usuario FROM producto", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                int precio = cursor.getInt(2);
                int cantidad = cursor.getInt(3);
                double latitud = cursor.getDouble(4);
                double longitud = cursor.getDouble(5);
                int idUsuario = cursor.getInt(6);
                Producto producto = new Producto(id, nombre, precio, cantidad, latitud, longitud, idUsuario, "Disponible");
                listaProductos.add(producto);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listaProductos;
    }
}