package com.example.wanderwand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.eftimoff.androipathview.PathView;
import com.example.wanderwand.login.LoginActivity;

import static utils.Constants.USER_TOKEN;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final PathView pathView = findViewById(R.id.pathView);
        pathView.getPathAnimator()
                .delay(1000)
                .duration(1000)
                .interpolator(new AccelerateDecelerateInterpolator())
                .start();

        pathView.useNaturalColors();
        pathView.setFillAfter(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent i;
        if (mSharedPreferences.getString(USER_TOKEN, null) != null) {
            i = MainActivity.getStartIntent(SplashActivity.this);
        } else {
            i = LoginActivity.getStartIntent(SplashActivity.this);
        }

        // TODO :: check for the user_token here & redirect to corresponding class
        // If token is null -> LoginActivity, else MainActivity
        new Handler().postDelayed(() -> {
            startActivity(i);
            finish();
        }, 2000);
    }
}
