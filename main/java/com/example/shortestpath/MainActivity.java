package com.example.shortestpath;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // Declare UI elements and database helper
    EditText username, password;
    Button loginButton, signupButton;
    ImageButton togglePasswordButton;
    DatabaseHelper databaseHelper;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        togglePasswordButton = findViewById(R.id.eyeButton); // Reference to the eye button

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Check for internet connection (optional)
        if (!Checkinternate.isConnected(this)) {
            Toast.makeText(this, "No internet connection. Redirecting to settings...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        }

        // Handle sign-up button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to move to SignUpActivity
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString();

                // Check if the user exists in the database
                boolean isLoggedIn = databaseHelper.checkUser(user, pass);
                if (isLoggedIn) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    // Start ShipRouteActivity after successful login
                    Intent intent = new Intent(MainActivity.this, AddRouteActivity.class);
                    startActivity(intent);
                    finish(); // Optionally call finish() to remove MainActivity from the back stack
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Toggle password visibility
        togglePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Hide password
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordButton.setImageResource(R.drawable.closed_eye_button); // Change to closed eye icon
                    isPasswordVisible = false;
                } else {
                    // Show password
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordButton.setImageResource(R.drawable.eye_button); // Change to open eye icon
                    isPasswordVisible = true;
                }
                // Move cursor to the end of the text
                password.setSelection(password.length());
            }
        });
    }
}
