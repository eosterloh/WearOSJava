package com.example.wearosjava;
import org.tensorflow.lite.Interpreter;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private static final float[] means =    {-6.347317f,   -3.6151795f ,  1.8212118f,  -0.0238195f,   0.02920377f, -0.02548838f};
    private static final float[] stddev =    {7.5649176f,  6.2568493f,  5.7432575f,  1.5803802f,  0.94720036f, 1.5644057f};



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
            dataArray[i] = (dataArray[i] - means[i])/stddev[i];
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
            ByteBuffer model = loadModelFile(context);
            if (model == null) return;
            tflite = new Interpreter(model);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing interpreter", e);
        }
    }

    /**
     * Load model from assets into a direct ByteBuffer with native order (required by TFLite).
     * Model file must be in mobile/src/main/assets/
     */
    private ByteBuffer loadModelFile(Context context) throws IOException {
        try (InputStream is = context.getAssets().open(modelName);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int n;
            while ((n = is.read(chunk)) != -1) out.write(chunk, 0, n);
            byte[] bytes = out.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes);
            buffer.rewind();
            return buffer;
        }
    }

}
