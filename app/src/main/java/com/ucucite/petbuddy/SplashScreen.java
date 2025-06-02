package com.ucucite.petbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Find the view you want to animate
        ImageView logoImage = findViewById(R.id.logoImage);

        // Load the animation resource
        Animation popOut = AnimationUtils.loadAnimation(this, R.anim.pop_out);

        // Apply the animation to the view
        logoImage.startAnimation(popOut);

        // Set an animation listener to start the OnboardingActivity after the animation ends
        popOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start the OnboardingActivity
                Intent intent = new Intent(SplashScreen.this, Onboarding.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });
    }
}