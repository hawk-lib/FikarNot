package com.fikarnot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.fikarnot.ui.authentication.LockScreenActivity;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashActivity.this.finish();
                if (SharedPrefManager.getInstance(SplashActivity.this).getUid().equals("")){
                    startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
                }else if (SharedPrefManager.getInstance(SplashActivity.this).getName().equals("")) {
                    startActivity(new Intent(SplashActivity.this, ProfileSetupActivity.class));
                }else {
                    //startActivity(new Intent(SplashActivity.this, VerificationActivity.class));
                    startActivity(new Intent(SplashActivity.this, LockScreenActivity.class));
                }
            }
        },1000);
    }
}