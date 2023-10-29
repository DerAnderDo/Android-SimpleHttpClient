package com.example.mystromerino;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String URL1 = "http://192.168.2.24:8000/data";
    private static final String BASE_URL = "http://192.168.2.24:8000/power=";

    private TextView responseTextView;
    private LineChart lineChart;
    private Handler handler;
    private boolean autoDataEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        responseTextView = findViewById(R.id.responseTextView);
        lineChart = findViewById(R.id.lineChart);
        Switch switchAutoData = findViewById(R.id.switchAutoData);
        handler = new Handler();

        switchAutoData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoDataEnabled = isChecked;
                if (isChecked) {
                    // If the switch is checked, start automatic data fetching
                    startAutoDataFetching();
                } else {
                    // If the switch is unchecked, stop automatic data fetching
                    stopAutoDataFetching();
                }
            }
        });

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
                            // Get the "power" value from the JSON object
                            int powerValue = jsonObject.getInt("power");
                            // Get the "currentThreshold" value from the JSON object
                            int currentThreshold = jsonObject.getInt("currentThreshold");
                            // Update the line chart with the received power value
                            updateLineChart(powerValue);
                            // Display the formatted JSON response in the TextView
                            responseTextView.setText("Power: " + powerValue + "\n" + "currentThreshold: " + currentThreshold);
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
                        // Handle errors
                        responseTextView.setText("Error occurred, please try again later.");
                    }
                });
        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void updateLineChart(int powerValue) {
        // Get the LineData object from the LineChart
        LineData data = lineChart.getData();

        // If LineData object is null, create a new one
        if (data == null) {
            data = new LineData();
            lineChart.setData(data);
        }

        // Get the DataSet from LineData, if null, create a new one
        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
        if (set == null) {
            set = new LineDataSet(null, "Power Data");
            set.setColor(Color.BLUE);
            set.setCircleColor(Color.BLUE);
            set.setLineWidth(2f);
            set.setCircleRadius(4f);
            data.addDataSet(set);
        }

        // Add a new entry to the DataSet using powerValue as the y-axis value
        data.addEntry(new Entry(set.getEntryCount(), powerValue), 0);

        // Notify the chart that the data has changed
        lineChart.notifyDataSetChanged();

        // Refresh the chart
        lineChart.invalidate();
    }

    private void startAutoDataFetching() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Perform HTTP GET request 1
                makeGetRequest(URL1);
                // Repeat this runnable every 30 seconds if autoDataEnabled is true
                if (autoDataEnabled) {
                    handler.postDelayed(this, 30000); // 30 seconds delay
                }
            }
        }, 30000); // 30 seconds initial delay
    }

    private void stopAutoDataFetching() {
        // Remove any existing callbacks to stop the automatic data fetching
        handler.removeCallbacksAndMessages(null);
    }
}
