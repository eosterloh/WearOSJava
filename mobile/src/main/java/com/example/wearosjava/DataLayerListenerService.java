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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(ACCEL_DATA_PATH)){
            byte[] data = messageEvent.getData();
            String message = new String(data);
            Log.d(TAG, "Received message: " + message);

            Intent intent = new Intent("wearable_data_event");
            intent.putExtra("message", message);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }else {
            super.onMessageReceived(messageEvent);
        }
    }
}
