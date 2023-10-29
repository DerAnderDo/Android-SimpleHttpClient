package com.example.mystromerino;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String URL1 = "http://192.168.2.24:8000/data";
    private static final String BASE_URL = "http://192.168.2.24:8000/power=";


    private TextView responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        responseTextView = findViewById(R.id.responseTextView);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform HTTP GET request 1
                makeGetRequest(URL1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input from EditText
                EditText numberEditText = findViewById(R.id.numberEditText);
                String userInput = numberEditText.getText().toString();
                // Validate user input
                int powerValue;
                try {
                    powerValue = Integer.parseInt(userInput);
                    if (powerValue < 0 || powerValue > 200) {
                        // Show an error message if the input is out of range
                        responseTextView.setText("Please enter a number between 0 and 200.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Show an error message if the input is not a valid number
                    responseTextView.setText("Invalid input. Please enter a number between 0 and 200.");
                    return;
                }
                // Construct the URL with user input
                String finalUrl = BASE_URL + powerValue;
                // Perform HTTP GET request with the final URL
                makeGetRequest(finalUrl);
            }
        });

    }
    private void makeGetRequest(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            // Get keys from the JSON object
                            Iterator<String> keys = jsonObject.keys();
                            // Display keys and their corresponding values line by line
                            StringBuilder formattedResponse = new StringBuilder();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = jsonObject.getString(key);
                                formattedResponse.append(key).append(": ").append(value).append("\n");
                            }
                            // Display the formatted JSON response in the TextView
                            responseTextView.setText(formattedResponse.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error
                            responseTextView.setText("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Display a detailed error message
                        String errorMessage;
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            errorMessage = new String(error.networkResponse.data);
                        } else {
                            errorMessage = "Error occurred, please try again later.";
                        }
                        responseTextView.setText(errorMessage);
                    }
                });
        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}