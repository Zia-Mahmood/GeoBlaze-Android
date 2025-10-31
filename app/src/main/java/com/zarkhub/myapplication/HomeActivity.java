package com.zarkhub.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void Go_back(View v){
        HomeActivity.this.finish();
    }

    public void Directions(View v){
        Intent intent = new Intent(HomeActivity.this,MapsActivity.class);
        HomeActivity.this.startActivity(intent);
    }

    public void Analysis(View v){

    }

    public void Test(View v){
        Intent intent = new Intent(HomeActivity.this,Test.class);
        HomeActivity.this.startActivity(intent);
    }
}