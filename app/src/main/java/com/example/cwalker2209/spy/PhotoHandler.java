package com.example.cwalker2209.spy;

/**
 * Created by C on 2018-01-07.
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import org.json.JSONObject;

public class PhotoHandler implements PictureCallback {

    private final Context context;
    private String filename;
    public String color;
    List<ClassResult> classResults;

    public PhotoHandler(Context context, String color) {
        this.context = context;
        this.color = color;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(CameraActivity.DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(CameraActivity.DEBUG_TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new apiCall().execute();
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }

    private void processResult(){
        boolean success = false;
        float score = 0;
        String last = "Last photo:";
        for (ClassResult result: classResults) {
            String name = result.getClassName();
            last += "\n" + name;
            if (name.contains(color)){
                score = result.getScore()*1000;
                success = true;
            }
        }
        if (success){
            Toast.makeText(context, "Sucessful Match! Score +" + score,
                    Toast.LENGTH_LONG).show();
            App.get().score += score;
            App.get().newColor = true;
        }
        else {
            Toast.makeText(context, "No Match! Score 0",
                    Toast.LENGTH_LONG).show();
        }
        App.get().lastItem = last;
        App.get().newColor = true;
    }

    private class apiCall extends AsyncTask<URL, Integer, Boolean> {

        protected Boolean doInBackground(URL... urls) {
            VisualRecognition service;
            InputStream imagesStream;

            service = new VisualRecognition(
                    VisualRecognition.VERSION_DATE_2016_05_20
            );
            service.setApiKey("5ffc243476a6b4bd994c34ae49e6e57f04bf72b7");

            try{
              imagesStream = new FileInputStream(filename);
            }
            catch (Exception e){
                Log.e("LOAD", e.getMessage());
                return false;
            }

            ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                    .imagesFile(imagesStream)
                    .imagesFilename(filename)
                    //.parameters("{\"classifier_ids\": [\"fruits_1462128776\",
                    //        + \"SatelliteModel_6242312846\"]}")
                    .build();
            ClassifiedImages results = service.classify(classifyOptions).execute();
            System.out.println(results);
            classResults = results.getImages().get(0).getClassifiers().get(0).getClasses();
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (success){
                processResult();
            }
        }
    }

}
