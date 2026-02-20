package com.example.wearosjava.presentation;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.example.wearosjava.R; // Correct import for your R file
import android.widget.TextView;
import android.view.WindowManager;

public class WearMainActivity extends ComponentActivity implements SensorDataUpdateListener{
    private SensorService sensorService;
    private TextView accelTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_wear_main); // Use an XML layout file
        sensorService = new SensorService(this,this);

        accelTextView = findViewById(R.id.accel_data_text);
        accelTextView.setText("Data Streaming ...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start listening for sensor data when the app is in the foreground
        sensorService.startListen();
    }


    //Thinking about removing this, keep sending data even when watch is sleeping, may be asleep during swing.
    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening when the app goes into the background to save battery
        sensorService.stopListen();
    }

    @Override
    public void onDataUpdated(String data) {
        runOnUiThread(() -> {
            if (accelTextView != null) {
                accelTextView.setText(formatDisplayData(data));
            }
        });
    }

    public String formatDisplayData(String csvData){
        String[] parts = csvData.split(",");
        if (parts.length >= 8) {
            // Accel X is at index 1
            String accelX = String.format("%.2f", Float.parseFloat(parts[1]));
            // Heart Rate is at index 7
            String heartRate = parts[7];


            // Construct a clean, readable display string
            return "STATUS:\n" +
                    "Accel X: " + accelX + "\n" +
                    "HR: " + heartRate + " BPM\n" +
                    "GPS (Active)";
        }
        return "Data Streaming ...";
    }
}