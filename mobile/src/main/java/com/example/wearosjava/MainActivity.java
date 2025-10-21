package com.example.wearosjava;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends ComponentActivity{
    private TextView receivedDataTextView;
    private DataReceiver dataReceiver;
    private DataProccessor dataProccessor;
    private Button labelSwingButton;
    private boolean isSwingInProgress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure your phone's activity_main.xml has a TextView with this ID
        receivedDataTextView = findViewById(R.id.received_data_text);

        labelSwingButton = findViewById(R.id.label_swing_button);

        dataReceiver = new DataReceiver();
        dataProccessor = new DataProccessor(getApplicationContext());

        labelSwingButton.setText("START SWING");

        labelSwingButton.setOnClickListener(v -> {
            if (!isSwingInProgress) {
                // STATE 1: Start Swing
                dataProccessor.logSwing("START_SWING");
                isSwingInProgress = true;
                labelSwingButton.setText("END SWING");
                receivedDataTextView.setText("SWING STARTED! Logging boundary...");
            } else {
                // STATE 2: End Swing
                dataProccessor.logSwing("END_SWING");
                isSwingInProgress = false;
                labelSwingButton.setText("START SWING");
                receivedDataTextView.setText("SWING ENDED. Ready for next swing.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver to listen for messages from the WearableListenerService
        LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, new IntentFilter("wearable_data_event"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver to avoid memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver);
    }

    private class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null) {
                receivedDataTextView.setText(message);
            }
        }
    }
}
