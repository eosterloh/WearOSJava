package com.example.wearosjava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerListener";
    private static final String ACCEL_DATA_PATH = "/accel_data";
    private static final String LABEL_DATA_PATH = "/swing_label";

    private DataProccessor dataProccessor;
    private SwingClassifier swingClassifier;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: starting");
        try {
            dataProccessor = new DataProccessor(getApplicationContext());
            Log.d(TAG, "onCreate: DataProccessor created");
            dataProccessor.startTrainingSession();
            Log.d(TAG, "onCreate: training session started");
            swingClassifier = new SwingClassifier(getApplicationContext());
            Log.d(TAG, "onCreate: SwingClassifier created");
        } catch (Throwable t) {
            Log.e(TAG, "onCreate: crash during init", t);
            throw t;
        }
        Log.d(TAG, "onCreate: finished");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: path=" + messageEvent.getPath() + " dataLength=" + (messageEvent.getData() != null ? messageEvent.getData().length : 0));
        byte[] data = messageEvent.getData();
        if (data == null) {
            Log.e(TAG, "onMessageReceived: data is null");
            return;
        }
        String message;
        try {
            message = new String(data);
        } catch (Throwable t) {
            Log.e(TAG, "onMessageReceived: failed to decode message", t);
            return;
        }

        if (messageEvent.getPath().equals(ACCEL_DATA_PATH)){
            Log.v(TAG, "Received message: " + message);

            // NEW USAGE: Write the data to the local file
            if (dataProccessor != null) {
                dataProccessor.appendRawData(message);
            }
            if (swingClassifier != null){
                swingClassifier.addData(message);
            }
            Intent intent = new Intent("wearable_data_event");
            String heartRate = parseHeartRateFromRaw(message);
            intent.putExtra("message", "Data live:"+ heartRate);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            if (swingClassifier.isSwing()){
                dataProccessor.logSwing("swing");
                //Phone Screen turns green

            }
        } /*else if (messageEvent.getPath().equals(LABEL_DATA_PATH)) { DEPRECATED: for training only
            Log.d(TAG, "Received swing label: " + message);
            if (dataProccessor != null) {
                // Log the label event. The DataProccessor generates the timestamp.
                dataProccessor.logSwing(message);
            }*/
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (dataProccessor != null){
            dataProccessor.endSession();
        }
    }

    private String parseHeartRateFromRaw(String rawData) {
        try {
            // The raw data format is: timestamp,ax,ay,az,gx,gy,gz,hr,steps,lat,lon
            String[] parts = rawData.split(",");
            if (parts.length >= 8) {
                // Heart Rate is the 8th element (index 7)
                return parts[7];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing HR data: " + e.getMessage());
        }
        return "N/A";
    }
}
