package com.example.tune;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreen extends AppCompatActivity {
    boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the user is logged in based on shared preferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyTokenPrefs", Context.MODE_PRIVATE);
                String accessToken = sharedPreferences.getString("access_token", "");
//                String refreshToken = sharedPreferences.getString("refresh_token", "");

                Intent intent;
                if (!accessToken.equals("") ) {
                    // Redirect to MainActivity
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                } else {
                    // Redirect to LoginActivity
                    intent = new Intent(SplashScreen.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000L); //3000 L = 3 detik
    }
}