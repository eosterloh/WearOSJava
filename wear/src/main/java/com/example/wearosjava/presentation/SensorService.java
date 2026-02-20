package com.example.wearosjava.presentation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static android.content.Context.SENSOR_SERVICE;

public class SensorService implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;
    private SensorDataUpdateListener listener;
    private SwingDetectorNative swingDetector;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private static final String TAG = "SensorService";
    private static final String ACCEL_DATA_PATH = "/accel_data";
    private static final int SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_NORMAL;
    private static final int MAX_REPORT_LATENCY_US = 0;

    public SensorService(Context context, SensorDataUpdateListener listener) {
        this(context, listener, null);
    }

    public SensorService(Context context, SensorDataUpdateListener listener, SwingDetectorNative swingDetector) {
        this.context = context;
        this.listener = listener;
        this.swingDetector = swingDetector;
        if (context != null) {
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        }
        sensorThread = new HandlerThread("SensorThread");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    public void startListen() {
        if (sensorManager != null) {
            if (accelerometer == null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }

            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SAMPLING_PERIOD_US, MAX_REPORT_LATENCY_US, sensorHandler);
                Log.d(TAG, "SUCCESS: Accelerometer listener registered.");
            } else {
                Log.e(TAG, "FAILURE: Accelerometer sensor was not found on this device.");
            }
        }
    }

    public void stopListen() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Listener is unregistered.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String payload = String.format("X: %.2f\nY: %.2f\nZ: %.2f", x, y, z);

            // Notify the listener with the new data
            if (listener != null) {
                listener.onDataUpdated(payload);
            }

            if (swingDetector != null) {
                swingDetector.addSample(x, y, z);
            }

            sendMessageToPhone(payload.getBytes());
        }
    }

    public void sendMessageToPhone(byte[] payload) {
        new Thread(() -> {
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
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}