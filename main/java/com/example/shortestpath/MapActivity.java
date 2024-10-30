package com.example.shortestpath;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;


import java.util.Locale;

public class MapActivity extends FragmentActivity {

    private MapView map;
    private GeoPoint origin;
    private GeoPoint destination;
    private String selectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        // Initialize the MapView and set the OpenSeaMap tile source
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.OPEN_SEAMAP);  // Use OpenSeaMap tile source for nautical purposes
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Initialize map controller and set default zoom
        IMapController mapController = map.getController();
        mapController.setZoom(9.0);

        // Example: Set an initial maritime location (e.g., Netherlands, for maritime reference)
        GeoPoint startPoint = new GeoPoint(52.2297, 4.4185);
        mapController.setCenter(startPoint);

        // Retrieve the selection type (origin or destination) from the previous activity
        selectionType = getIntent().getStringExtra("selectionType");

        // Handle map click events for setting origin and destination
        map.setOnClickListener(v -> {
            GeoPoint clickedPoint = (GeoPoint) map.getProjection().fromPixels((int) v.getX(), (int) v.getY());
            if ("origin".equals(selectionType)) {
                if (origin == null) {
                    origin = clickedPoint;
                    addMarker(origin, "Origin");
                    getWeatherData(origin.getLatitude(), origin.getLongitude()); // Fetch weather for origin
                }
            } else if ("destination".equals(selectionType)) {
                if (destination == null) {
                    destination = clickedPoint;
                    addMarker(destination, "Destination");
                    getWeatherData(destination.getLatitude(), destination.getLongitude()); // Fetch weather for destination
                }
            }
        });
    }

    // Method to add a marker on the map
    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(title);
        map.getOverlays().add(marker);
    }

    // Method to fetch and display weather data (you can implement the actual weather fetching logic)
    private void getWeatherData(double latitude, double longitude) {
        Toast.makeText(this, String.format(Locale.getDefault(), "Fetching weather for %.4f, %.4f", latitude, longitude), Toast.LENGTH_SHORT).show();
        // Add the weather API call logic here (similar to how it was done in the previous implementation)
    }
}
