package com.rsin.socketiochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.flaviofaria.kenburnsview.Transition;
import com.flaviofaria.kenburnsview.TransitionGenerator;

public class StaterActivity extends AppCompatActivity {
    Button button;
    KenBurnsView kbv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stater);
        kbv = findViewById(R.id.image_starter);


        Glide.with(getApplicationContext()).load("https://drive.google.com/uc?id=1ubrCL-yWU4hDSeLrsA3nS-8SEOUUhw8K").placeholder(R.drawable.mountains).into(kbv);

        kbv.setTransitionListener(new KenBurnsView.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }
            @Override
            public void onTransitionEnd(Transition transition) {

            }
        });

        AccelerateDecelerateInterpolator adi = new AccelerateDecelerateInterpolator();
//        Interpolator interpolator = new PathInterpolator(0,0);
        RandomTransitionGenerator generator = new RandomTransitionGenerator(3000, adi);
        kbv.setTransitionGenerator(generator);

        button = findViewById(R.id.start_talking_button);
        button.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        });

    }
}