package com.example.learning_assistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class Splash extends AppCompatActivity {
SharedPreferences sharedPreferences;
Boolean firstTime;
    private static int SPLASH_TIME_OUT = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        firstTime = sharedPreferences.getBoolean("firstTime", true);
       if (firstTime)
       {
           new Handler().postDelayed(new Runnable() {
                                         @Override
                                         public void run() {

                                             SharedPreferences.Editor editor = sharedPreferences.edit();
                                             firstTime = false;
                                             editor.putBoolean("firstTime", firstTime);
                                             editor.apply();
                                             Intent goMain = new Intent(Splash.this, MainActivity.class);
                                             startActivity(goMain);
                                             Animatoo.animateFade(Splash.this);
                                             finish();
                                         }
                                     },SPLASH_TIME_OUT
           );
       }
       else
       {
           Intent goMain = new Intent(Splash.this, MainActivity.class);
           startActivity(goMain);
           finish();
       }

    }
}
