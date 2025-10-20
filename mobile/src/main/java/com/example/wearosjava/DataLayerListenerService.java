package com.example.wearosjava;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListenerService extends WearableListenerService{
    private static final String TAG = "DataLayerListener";
    private static final String ACCEL_DATA_PATH = "/accel_data";

    private DataProccessor dataProccessor;

    @Override
    public void onCreate(){
        super.onCreate();
        dataProccessor = new DataProccessor(getApplicationContext());
        dataProccessor.startTrainingSession();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(ACCEL_DATA_PATH)){
            byte[] data = messageEvent.getData();
            String message = new String(data);
            Log.d(TAG, "Received message: " + message);

            // NEW USAGE: Write the data to the local file
            if (dataProccessor != null) {
                dataProccessor.appendRawData(message);
            }

            Intent intent = new Intent("wearable_data_event");
            String heartRate = parseHeartRateFromRaw(message);
            intent.putExtra("message", "Data live:"+ heartRate);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }else {
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
