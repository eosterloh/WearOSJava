package com.example.wearosjava.presentation;

public class SwingDetectorNative {
    private final SwingDetectionListener listener;
    private boolean isSwinging;
    private long lastSwingTimeMs;
    private static final float MAGNITUDE_THRESHOLD = 18f;
    private static final long COOLDOWN_MS = 1500;

    public SwingDetectorNative(SwingDetectionListener listener) {
        this.listener = listener;
        this.isSwinging = false;
        this.lastSwingTimeMs = 0;
    }

    /**
     * Feed an accelerometer sample. Calls the listener once when magnitude exceeds
     * threshold, then enforces a cooldown before the next detection.
     */
    public void addSample(float x, float y, float z) {
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        long now = System.currentTimeMillis();
        if (magnitude >= MAGNITUDE_THRESHOLD && (now - lastSwingTimeMs) >= COOLDOWN_MS) {
            isSwinging = true;
            lastSwingTimeMs = now;
            if (listener != null) {
                listener.onSwingDetected();
            }
        } else {
            isSwinging = false;
        }
    }
}
