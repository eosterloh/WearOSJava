package com.example.wearosjava;
import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
public class SwingClassifier {
    //Good DS for the buffer? linkedlist or arraylist. Using arraylist
    private static final String TAG = "SwingClassfier";
    public ArrayList<float[]> buffer = new ArrayList<>();

    private Interpreter tflite;
    private String modelName = "attempt1bestLITE.tflite";
    private static final int WINDOWSIZE = 800;
    private static final int NUMFEATURES = 6;
    private static final float THRESHOLD = 0.8f;



    public SwingClassifier(Context context) {
        //Things to add here: initialize identifier
        initializeInterpreter(context);
    }

    /**
     * Adds data to the buffer from the broadcasted watch data
     * @param data
     */
    public void addData(String data) {
        //Parse data and add to buffer
        String[] parts = data.split(",");
        float[] dataArray = new float[NUMFEATURES];
        for(int i = 0; i<NUMFEATURES; i++){
            dataArray[i] = Float.parseFloat(parts[i]);
        }
        buffer.add(dataArray);

        while(buffer.size()>800) {
            buffer.remove(0);//FIFO
        }
    }



    public boolean isSwing(){
        if (buffer.size() < WINDOWSIZE) {
            Log.d(TAG, "Buffer not full yet: " + buffer.size());
            return false;
        }

        float[][][] inputArr = new float[1][WINDOWSIZE][NUMFEATURES];


        for(int i=0; i<WINDOWSIZE; i++){
            inputArr[0][i] = buffer.get(i);
        }

        float[][] outputArr  = new float[1][1];

        if(tflite != null){
            tflite.run(inputArr, outputArr);
        }

        return outputArr[0][0] > THRESHOLD;
    }

    private void initializeInterpreter(Context context) {
        try {
            MappedByteBuffer model = loadModelFile(context);
            tflite = new Interpreter(model);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing interpreter:" + e.getMessage());
        }
    }

    /**
     * Helper function for loading the model preparing for inference.
     * @param context the context that the identificaiton is happening
     * @return
     * @throws IOException
     */
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        try{
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            Log.e(TAG, "Error loading model file" + e.getMessage());
        }
        return null;
    }

}
