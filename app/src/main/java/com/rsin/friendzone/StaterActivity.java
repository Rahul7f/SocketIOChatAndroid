package com.rsin.friendzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
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

        kbv.setImageDrawable(getDrawable(R.drawable.bg2));
//        Glide.with(getApplicationContext()).load("https://drive.google.com/uc?id=1ubrCL-yWU4hDSeLrsA3nS-8SEOUUhw8K").placeholder(R.drawable.mountains).into(kbv);

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
            SharedPreferences.Editor editor = getSharedPreferences("PREFS_STATER", MODE_PRIVATE).edit();
            editor.putBoolean("VALUE",true);
            editor.apply();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        });

    }

    @Override
    protected void onStart() {
        SharedPreferences prefs = getSharedPreferences("PREFS_STATER", MODE_PRIVATE);
        boolean value = prefs.getBoolean("VALUE",false);//"No name defined" is the default value.
        if(value)
        {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        }
        super.onStart();
    }
}