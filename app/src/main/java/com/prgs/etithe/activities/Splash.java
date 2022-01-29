package com.prgs.etithe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.prgs.etithe.R;
import com.prgs.etithe.utilities.Global;

public class Splash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spalsh);

        Thread splashThread = new Thread() {
            public void run() {
                try {
                    sleep(5 * 1000);
                    SharedPreferences sharedPref = getSharedPreferences(Global._ETITHE_REMEMBER_ME, MODE_PRIVATE);
                    boolean saveLogin = sharedPref.getBoolean("remember_me", false);
                    if (!saveLogin) {
                        Intent mainIntent = new Intent(Splash.this, Login.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        if (Global.USER_CODE != null) {
                            Global.GET_LOGIN_INFO_FROM_MEMORY(getApplicationContext());
                            if (Global.LOGIN_USER_DETAIL != null) {
                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                                finish();
                            } else {
                                Intent mainIntent = new Intent(getApplicationContext(), Login.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        } else {
                            Intent mainIntent = new Intent(getApplicationContext(), Login.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        };
        splashThread.start();
    }


}
