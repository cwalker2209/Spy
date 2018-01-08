package com.example.cwalker2209.spy;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {
    public final static String DEBUG_TAG = "CameraActivity";
    private Camera camera;
    private CameraPreview cameraPreview;
    private int cameraId = 0;
    private String[] colors;
    private String color = "red";
    private float score = 0;

    private TextView textViewColor;
    private TextView textViewScore;
    private TextView textViewNames;

    // Create the Handler object (on the main thread by default)
    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            updateResults();
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 2000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textViewColor = findViewById(R.id.textViewColor);
        textViewScore = findViewById(R.id.textViewScore);
        textViewNames = findViewById(R.id.textViewNames);


        //If authorisation not granted for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED  || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //ask for authorisation
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }
        else {
            setUpCamera();
        }

        Random rng = new Random();
        colors =  new String[]{"red","blue","green","yellow","brown","black","white","purple","orange","grey"};
        color = colors[rng.nextInt(colors.length)];
        textViewColor.setText("I spy something the color "+ color);
        textViewScore.setText("Score: "+ score);

        score = App.get().score;
        runnableCode.run();
    }

    public void setUpCamera(){
        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findBackFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
                FrameLayout frame = findViewById(R.id.cameraFrame);
                cameraPreview = new CameraPreview(this, camera, cameraId);
                frame.addView(cameraPreview);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 50: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setUpCamera();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG)
                            .show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onClick(View view) {
        try {
            camera.takePicture(null, null,
                    new PhotoHandler(getApplicationContext(), color));
        }
        catch (Exception e){
            Log.e("Camera", e.getMessage());
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.stopPreview();
            //camera.release();
            //camera = null;
        }
        super.onPause();
    }

    protected void onRestart() {
        super.onRestart();
        setUpCamera();
    }

    public void updateResults(){
        if (App.get().newColor){
            score = App.get().score;

            Random rng = new Random();
            colors =  new String[]{"red","blue","green","yellow","brown","black","white","purple","orange","grey"};
            color = colors[rng.nextInt(colors.length)];

            textViewColor.setText("I spy something the color "+ color);
            textViewScore.setText("Score: "+ score);
            textViewNames.setText(App.get().lastItem);


            camera.startPreview();

            App.get().newColor = false;
        }
    }



}
