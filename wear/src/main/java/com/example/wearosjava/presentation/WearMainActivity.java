package com.example.wearosjava.presentation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import com.example.wearosjava.R;

public class WearMainActivity extends ComponentActivity implements SensorDataUpdateListener {
    private static final long SWING_FEEDBACK_DELAY_MS = 1500;

    private SensorService sensorService;
    private SwingDetectorNative swingDetector;
    private TextView accelTextView;
    private View contentRoot;
    private int originalBackgroundColor;
    private Runnable resetFeedbackRunnable;
    private boolean showingSwingFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        accelTextView = findViewById(R.id.accel_data_text);
        contentRoot = findViewById(android.R.id.content);
        if (contentRoot != null) {
            originalBackgroundColor = Color.BLACK;
        }

        swingDetector = new SwingDetectorNative(this::onSwingDetected);
        sensorService = new SensorService(this, this, swingDetector);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorService.startListen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (resetFeedbackRunnable != null && contentRoot != null) {
            contentRoot.removeCallbacks(resetFeedbackRunnable);
        }
        sensorService.stopListen();
    }

    private void onSwingDetected() {
        runOnUiThread(() -> {
            showingSwingFeedback = true;
            if (contentRoot != null) {
                contentRoot.setBackgroundColor(Color.GREEN);
            }
            if (accelTextView != null) {
                accelTextView.setText("Swing!");
            }
            if (resetFeedbackRunnable != null && contentRoot != null) {
                contentRoot.removeCallbacks(resetFeedbackRunnable);
            }
            resetFeedbackRunnable = () -> {
                showingSwingFeedback = false;
                if (contentRoot != null) {
                    contentRoot.setBackgroundColor(originalBackgroundColor);
                }
                if (accelTextView != null) {
                    accelTextView.setText("Tracking Sensor Data...");
                }
            };
            if (contentRoot != null) {
                contentRoot.postDelayed(resetFeedbackRunnable, SWING_FEEDBACK_DELAY_MS);
            }
        });
    }

    @Override
    public void onDataUpdated(String data) {
        runOnUiThread(() -> {
            if (!showingSwingFeedback && accelTextView != null) {
                accelTextView.setText("Accel Data: \n" + data);
            }
        });
    }
}