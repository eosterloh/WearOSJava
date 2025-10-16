package com.example.wearosjava.presentation;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.example.wearosjava.R; // Correct import for your R file
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log; // Import Log for debugging
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static android.content.Context.SENSOR_SERVICE;
import android.widget.TextView;

public class WearMainActivity extends ComponentActivity implements SensorDataUpdateListener{
    private SensorService sensorService;
    private TextView accelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main); // Use an XML layout file
        sensorService = new SensorService(this,this);

        accelTextView = findViewById(R.id.accel_data_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start listening for sensor data when the app is in the foreground
        sensorService.startListen();
    }

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
                accelTextView.setText("Accel Data: \n" + data);
            }
        });
    }
}