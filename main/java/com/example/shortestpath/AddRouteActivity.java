package com.example.shortestpath;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AddRouteActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText originInput, destinationInput, waypointsInput, fuelInput, travelTimeInput, comfortInput;
    private Button addRouteButton, selectRouteButton;
    private DatabaseHelperForShip databaseHelper;
    private ImageView originMapIcon, destinationMapIcon;
    private TextView weatherTextView; // TextView to display weather info

    // Location and weather variables
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int MAP_REQUEST_CODE_ORIGIN = 1; // Define constants for request codes
    private static final int MAP_REQUEST_CODE_DESTINATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        // Initialize UI elements
        originInput = findViewById(R.id.originInput);
        destinationInput = findViewById(R.id.destinationInput);
        waypointsInput = findViewById(R.id.waypointsInput);
        fuelInput = findViewById(R.id.fuelInput);
        travelTimeInput = findViewById(R.id.travelTimeInput);
        comfortInput = findViewById(R.id.comfortInput);
        addRouteButton = findViewById(R.id.addRouteButton);
        selectRouteButton = findViewById(R.id.selectRouteButton);

        // Initialize ImageView elements for map icons
        originMapIcon = findViewById(R.id.originMapIcon);
        destinationMapIcon = findViewById(R.id.destinationMapIcon);

        // Initialize TextView for weather info
        weatherTextView = findViewById(R.id.weather_info); // Make sure this ID exists in your layout

        // Initialize database helper
        databaseHelper = new DatabaseHelperForShip(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle add route button click
        addRouteButton.setOnClickListener(v -> addRoute());

        // Handle select route button click
        selectRouteButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddRouteActivity.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE_ORIGIN); // Use defined request code for selecting routes
        });

        // Update map icon click events to open MapActivity
        originMapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AddRouteActivity.this, MapActivity.class);
            intent.putExtra("selectionType", "origin"); // Specify that this is for selecting origin
            startActivityForResult(intent, MAP_REQUEST_CODE_ORIGIN); // Use the request code for origin
        });

        destinationMapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AddRouteActivity.this, MapActivity.class);
            intent.putExtra("selectionType", "destination"); // Specify that this is for selecting destination
            startActivityForResult(intent, MAP_REQUEST_CODE_DESTINATION); // Use the request code for destination
        });

        // Request location permission
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Fetch weather data for the current location
                        getWeatherData(latitude, longitude);
                    } else {
                        Toast.makeText(AddRouteActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getWeatherData(double latitude, double longitude) {
        String apiKey = getString(R.string.google_openwhether_api_key); // Replace with your actual key
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        final String weatherDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        final double temperature = json.getJSONObject("main").getDouble("temp");

                        runOnUiThread(() -> {
                            weatherTextView.setText("Weather: " + weatherDescription + ", Temp: " + temperature + "°C");
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void addRoute() {
        String origin = originInput.getText().toString().trim();
        String destination = destinationInput.getText().toString().trim();
        String waypoints = waypointsInput.getText().toString().trim();
        String fuelString = fuelInput.getText().toString().trim();
        String travelTimeString = travelTimeInput.getText().toString().trim();
        String comfort = comfortInput.getText().toString().trim();

        // Convert String to double
        double fuel;
        double travelTime;

        try {
            fuel = Double.parseDouble(fuelString);
            travelTime = Double.parseDouble(travelTimeString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for fuel and travel time.", Toast.LENGTH_SHORT).show();
            return; // Exit if the input is not valid
        }

        // Add the route to the database
        boolean isInserted = databaseHelper.insertRoute(origin, destination, waypoints, fuel, travelTime, comfort);
        if (isInserted) {
            Toast.makeText(this, "Route Added Successfully", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Failed to Add Route", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            LatLng selectedLocation = data.getParcelableExtra("location");

            if (requestCode == MAP_REQUEST_CODE_ORIGIN) {
                // Set the origin based on the result from MapActivity
                if (selectedLocation != null) {
                    originInput.setText(String.format("%s, %s", selectedLocation.latitude, selectedLocation.longitude));
                }
            } else if (requestCode == MAP_REQUEST_CODE_DESTINATION) {
                // Set the destination based on the result from MapActivity
                if (selectedLocation != null) {
                    destinationInput.setText(String.format("%s, %s", selectedLocation.latitude, selectedLocation.longitude));
                }
            }
            // Get weather data for the selected origin or destination
            getWeatherData(selectedLocation.latitude, selectedLocation.longitude);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
