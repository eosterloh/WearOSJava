package com.example.wearosjava.presentation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import android.location.Location;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

/**
 * This is the service that gathers all the features for the ML model that detects swings.
 */
public class SensorService implements SensorEventListener {
    // --- SENSOR DECLERATIONS ---
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor heartRateSensor;
    //private Sensor stepCounterSensor;
    private SensorDataUpdateListener listener; // Added listener field
    // Context for android
    private Context context;


    // --- GPS Declarations ---
    //private FusedLocationProviderClient fusedLocationClient;
    //private LocationCallback locationCallback;
    //private String latestGpsData = "0.0,0.0";
    // --- LAST KNOWN VALUE DECLERATIONS ---
    private float[] latestAccel = {0.0f, 0.0f, 0.0f}; // X, Y, Z
    private float[] latestGyro = {0.0f, 0.0f, 0.0f}; // X, Y, Z
    private float latestHeartRate = 0.0f;
    //private float latestSteps = 0.0f;
    //Paths of services
    private static final String TAG = "SensorService";
    private static final String ACCEL_DATA_PATH = "/accel_data";
    private static final String LABEL_DATA_PATH = "/swing_label";

    private ExecutorService executorService;

    public SensorService(Context context, SensorDataUpdateListener listener) { // Corrected constructor
        this.context = context;
        this.listener = listener; // Initialized listener
        if (context != null) {
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            //fusedLocationClient = LocationServices.getFusedLocationProviderClient(context); // Initialize GPS client
            //createLocationCallback();
        }
    }

    /*
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Update the latest known GPS data
                    //latestGpsData = location.getLatitude() + "," + location.getLongitude();
                    Log.d(TAG, "GPS Updated: " + latestGpsData);
                }
            }
        };
    }
    */
    public void startListen() {
        if (sensorManager != null) {
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
            }
            // 1. ACCELEROMETER (already done)
            if (accelerometer == null) accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST); // Use FASTEST for ML data

            // 2. GYROSCOPE
            if (gyroscope == null) gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscope != null) sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

            // 3. HEART RATE
            if (heartRateSensor == null) heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            if (heartRateSensor != null) sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

            // 4. STEP COUNTER
            //if (stepCounterSensor == null) stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            //if (stepCounterSensor != null) sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

            // 5. GPS LOCATION (Fused Location Provider)
            //LocationRequest locationRequest = new LocationRequest.Builder(5000) // Interval in milliseconds
              //      .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                //    .build();

            // Ensure you have permission granted here (usually done in MainActivity)
            // This is pseudo-code for the permission check:
        }
    }

    public void stopListen() {
        if (sensorManager != null) {
            Log.e(TAG, "SUCCESS: Listener is unregistered.");
            sensorManager.unregisterListener(this);

            //fusedLocationClient.removeLocationUpdates(locationCallback);
            if (executorService != null && !executorService.isShutdown()) {
                // Use shutdownNow() to interrupt pending tasks
                executorService.shutdownNow();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            latestAccel = event.values.clone();
            String payload = String.format("%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.1f",
                    timestamp,
                    latestAccel[0], latestAccel[1], latestAccel[2],
                    latestGyro[0], latestGyro[1], latestGyro[2],
                    latestHeartRate
                    //latestGpsData // Format is "Lat,Lon"
            );

            // Notify the listener with the new data
            if (listener != null) {
                listener.onDataUpdated(payload);
            }

            sendMessageToPhone(payload.getBytes());
        }// 2. GYROSCOPE: Just store the latest value
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            latestGyro = event.values.clone();
        }// 3. HEART RATE: Just store the latest value
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            latestHeartRate = event.values[0];
        }// 4. STEP COUNTER: Just store the latest value
        //else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        //  latestSteps = event.values[0];
        //}
    }

    public void sendMessageToPhone(byte[] payload) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.submit(() -> {
                try {
                    List<Node> connectedNodes = Tasks.await(Wearable.getNodeClient(context).getConnectedNodes());
                    for (Node node : connectedNodes) {
                        Task<Integer> sendTask = Wearable.getMessageClient(context).sendMessage(node.getId(), ACCEL_DATA_PATH, payload);
                        sendTask.addOnSuccessListener(integer -> Log.d(TAG, "Message sent successfully to " + node.getDisplayName()));
                        sendTask.addOnFailureListener(e -> Log.e(TAG, "Message failed to send.", e));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error finding connected nodes", e);
                }
            });
        }
    }

    public void sendLabelToPhone(String label) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.submit(() -> {
                try {
                    byte[] payload = label.getBytes();
                    List<Node> connectedNodes = Tasks.await(Wearable.getNodeClient(context).getConnectedNodes());
                    for (Node node : connectedNodes) {
                        // Send the message using the new label path
                        Task<Integer> sendTask = Wearable.getMessageClient(context).sendMessage(node.getId(), LABEL_DATA_PATH, payload);
                        sendTask.addOnSuccessListener(integer -> Log.d(TAG, "Label sent successfully: " + label));
                        sendTask.addOnFailureListener(e -> Log.e(TAG, "Label failed to send.", e));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error finding connected nodes to send label", e);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}