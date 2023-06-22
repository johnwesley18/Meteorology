package com.example.gptversionweatherproject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText locationEditText;
    private Button fetchWeatherButton;
    private TextView locationTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView errorTextView;

    private static final String API_KEY = "b1e49b581f5537302680d62f1e0b7ad9"; // Replace with your OpenWeatherMap API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        locationEditText = findViewById(R.id.locationEditText);
        fetchWeatherButton = findViewById(R.id.fetchWeatherButton);
        locationTextView = findViewById(R.id.locationTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windTextView = findViewById(R.id.windTextView);
        errorTextView = findViewById(R.id.errorTextView);

        // Set click listener for the fetch weather button
        fetchWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = locationEditText.getText().toString().trim();
                if (!location.isEmpty()) {
                    new FetchWeatherTask().execute(location);
                } else {
                    errorTextView.setText("Please enter a location.");
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String location = params[0];
            String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + API_KEY;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // Read data from the API response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
                inputStream.close();
                connection.disconnect();

                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);

                    // Extract relevant weather information from the JSON response
                    String location = json.getString("name");
                    JSONObject main = json.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    int humidity = main.getInt("humidity");
                    JSONObject wind = json.getJSONObject("wind");
                    double windSpeed = wind.getDouble("speed");

                    // Update the UI with the weather information
                    locationTextView.setText(location);
                    temperatureTextView.setText("Temperature: " + temperature + "°C");
                    humidityTextView.setText("Humidity: " + humidity + "%");
                    windTextView.setText("Wind Speed: " + windSpeed + " m/s");
                    errorTextView.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError("Failed to parse weather data.");
                }
            } else {
                showError("Failed to fetch weather data.");
            }
        }

        private void showError(String errorMessage) {
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            errorTextView.setText(errorMessage);
            locationTextView.setText("");
            temperatureTextView.setText("");
            humidityTextView.setText("");
            windTextView.setText("");
        }
    }
}
