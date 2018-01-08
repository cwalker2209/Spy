package com.example.cwalker2209.spy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void camera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void how(View view){
        Intent intent = new Intent(this, HowActivity.class);
        startActivity(intent);
    }

    public void about(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
