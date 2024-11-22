package com.example.marketplace;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;


public class AgregarProductosFragment extends Fragment {
    DataHelper dh;
    SQLiteDatabase bd;
    private MapView mapView;
    private ViewPager2 viewPager;
    private Marker currentMarker;

    Usuario usuario = Usuario.getInstance();

    EditText agregarNombreProducto, agregarPrecioProducto, agregarCantidadInicial, agregarNumeroDireccion;

    Button agregarBoton;

    TextView agregarLatitud, agregarLongitud;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agregar_productos, container, false);

        dh = new DataHelper(requireContext(), "app.db", null, 3);
        bd = dh.getWritableDatabase();

        agregarLatitud = view.findViewById(R.id.agregarLatitud);
        agregarLongitud = view.findViewById(R.id.agregarLongitud);

        // datos de los inputs
        agregarNombreProducto = view.findViewById(R.id.agregarNombreProducto);
        agregarPrecioProducto = view.findViewById(R.id.agregarPrecioProducto);
        agregarCantidadInicial = view.findViewById(R.id.agregarCantidadInicial);



        agregarBoton = view.findViewById(R.id.agregarBoton);

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));

        mapView = view.findViewById(R.id.agregarMapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        double ipSantoTomasLatitud = -33.4493141;
        double ipSantoTomasLongitud = -70.6624069;

        GeoPoint IpSantoTomasPoint = new GeoPoint(ipSantoTomasLatitud, ipSantoTomasLongitud);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(IpSantoTomasPoint);

        Spinner mapTypeSpinner = view.findViewById(R.id.spinnerMapa);
        String[] mapTypes = { "Mapa normal", "Mapa de transporte", "Mapa topográfico" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mapTypes){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);

        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position){
                    case 0:
                        mapView.setTileSource(TileSourceFactory.MAPNIK);
                        break;
                    // en la posición 1, mostrará el mapa de transporte público
                    case 1:
                        mapView.setTileSource(new XYTileSource(
                                "PublicTransport",
                                0,
                                18,
                                256,
                                ".png",
                                new String[]{"https://tile.memomaps.de/tilegen/"}
                        ));
                        break;
                    // en la posicion 2, mostrará un mapa topográfico
                    case 2:
                        mapView.setTileSource(new XYTileSource(
                                "ISGS_Satellite",
                                0,
                                18,
                                256,
                                ".png",
                                new String[]{
                                        "https://a.tile.opentopomap.org/",
                                        "https://b.tile.opentopomap.org/",
                                        "https://c.tile.opentopomap.org/"
                                }
                        ));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

        mapView.getOverlays().add(new Overlay() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e, @NonNull MapView mapView) {
                GeoPoint geoPoint = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());

                if (currentMarker != null) {
                    mapView.getOverlays().remove(currentMarker);
                }

                currentMarker = new Marker(mapView);
                currentMarker.setPosition(geoPoint);
                currentMarker.setTitle("Posición de venta");
                mapView.getOverlays().add(currentMarker);

                mapView.getController().animateTo(geoPoint);

                mapView.invalidate();

                agregarLatitud.setText(String.valueOf(geoPoint.getLatitude()));
                agregarLongitud.setText(String.valueOf(geoPoint.getLongitude()));

                return true;
            }
        });

        // agregar evento

        agregarBoton.setOnClickListener(v -> {
            String nombreProducto = agregarNombreProducto.getText().toString();
            String latitudString = agregarLatitud.getText().toString();
            String longitudString = agregarLongitud.getText().toString();

            if (nombreProducto.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, ingresa un nombre de producto.", Toast.LENGTH_SHORT).show();
                return; // Salir del método si hay un error
            }

            if (agregarPrecioProducto.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Por favor, ingresa un precio.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (agregarCantidadInicial.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Por favor, ingresa la cantidad inicial.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (latitudString.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, ingresa la latitud.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (longitudString.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, ingresa la longitud.", Toast.LENGTH_SHORT).show();
                return;
            }

            int idUsuario = usuario.getId();
            int precioProducto = Integer.parseInt(agregarPrecioProducto.getText().toString());
            int cantidadInicial = Integer.parseInt(agregarCantidadInicial.getText().toString());
            double latitud = Double.parseDouble(latitudString);
            double longitud = Double.parseDouble(longitudString);

            ContentValues values = new ContentValues();
            values.put("nombre", nombreProducto);
            values.put("precio", precioProducto);
            values.put("cantidad", cantidadInicial);
            values.put("latitud", latitud);
            values.put("longitud", longitud);
            values.put("id_usuario", idUsuario);

            long rowId = bd.insert("producto", null, values);

            if (rowId == -1) {
                Toast.makeText(getContext(), "No se pudo agregar el producto", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Producto agregado con éxito", Toast.LENGTH_LONG).show();
            }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
            mapView.invalidate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }


}