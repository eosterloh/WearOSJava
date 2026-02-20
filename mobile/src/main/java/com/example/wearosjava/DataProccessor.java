package com.example.wearosjava;

import android.provider.ContactsContract;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataProccessor {
    private static final String TAG = "DataProccessor";
    private File dataFile;
    private File labelFile;
    private FileWriter dataWriter;
    private FileWriter labelFileWriter;
    private final String filepath = "swingData.csv";
    private final String labelpath = "swingLabels.csv";
    private boolean labeledSwing;


    public DataProccessor(Context context){
        File appFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (appFilesDir != null && !appFilesDir.exists()) {
            appFilesDir.mkdirs();
        }
        // Constructor takes in the
        dataFile = new File(appFilesDir,filepath);
        labelFile = new File(appFilesDir,labelpath);
    }


    public void startTrainingSession(){
        try{
            dataWriter = new FileWriter(dataFile, true);
            labelFileWriter = new FileWriter(labelFile, true);

            if (dataFile.length() == 0) {
                // Note: The rawData from the watch does NOT include the final 'label' column.
                dataWriter.append("timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,heart_rate\n");
            }
            if (labelFile.length() == 0) {
                labelFileWriter.append("timestamp,event_type\n");
            }
            dataWriter.flush();
            labelFileWriter.flush();
            Log.i(TAG, "Training session started.");
        }catch (IOException e) {
            Log.e(TAG, "Error starting training session: " + e.getMessage());
        }
    }

    public void appendRawData(String rawData){
        if (dataWriter != null){
            try {
                dataWriter.append(rawData).append("\n");
                dataWriter.flush();
            }catch (IOException e){
                Log.e(TAG, "Error appending sensor data: " + e.getMessage());
            }
        }
    }

    public void logSwing(String event) {
        if (labelFileWriter != null) {
            try {
                long timestamp = System.currentTimeMillis();
                labelFileWriter.append(timestamp + "," + event + "\n");
                labelFileWriter.flush();
                Log.i(TAG, "Swing Event Logged");
            } catch (IOException e) {
                Log.e(TAG, "Error logging swing: " + e.getMessage());
            }
        } else {
            // NEW DIAGNOSTIC LINE: Tell yourself the button was pressed too soon!
            Log.e(TAG, "Error: Cannot log swing. File writer is NULL. Service not ready.");
        }
    }



    public void endSession() {
        if (dataWriter != null) {
            try {
                dataWriter.close();
                labelFileWriter.close();
                Log.i(TAG, "Training session ended.");
            } catch (IOException e) {
                Log.e(TAG, "Error closing file: " + e.getMessage());
            }
        }
   }

}
