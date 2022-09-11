package com.rsin.friendzone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputLayout editText;
    Button signInButton, stranger_btn;
    MaterialCardView music,tech,relationship,mental_health,looking_for_advice,other;
    TextView music_tt,tech_tt,relationship_tt,mental_health_tt,looking_for_advice_tt,other_tt;

    int currentHighLiteButton;
    int currentHighLiteText;
    String tagValue = "no";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editText =  findViewById(R.id.username_input);
        signInButton =  findViewById(R.id.sign_in_button);
        stranger_btn =  findViewById(R.id.stranger_option);

        music =  findViewById(R.id.tag_music);
        tech =  findViewById(R.id.tag_tech);
        relationship =  findViewById(R.id.tag_relationship);
        mental_health =  findViewById(R.id.tag_mental_health);
        looking_for_advice =  findViewById(R.id.tag_looking_for_advice);
        other =  findViewById(R.id.tag_other);

        music_tt =  findViewById(R.id.music_tt);
        tech_tt =  findViewById(R.id.tech_tt);
        relationship_tt =  findViewById(R.id.relation_tt);
        mental_health_tt =  findViewById(R.id.mh_tt);
        looking_for_advice_tt =  findViewById(R.id.loc_tt);
        other_tt =  findViewById(R.id.other_tt);

        currentHighLiteButton = R.id.tag_other;
        currentHighLiteText = R.id.other_tt;

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getEditText().getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "enter your name", Toast.LENGTH_SHORT).show();
                }
                else if(tagValue.equals("no"))
                {
                    Toast.makeText(getApplicationContext(), "Select Tag", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    attemptLogin(editText.getEditText().getText().toString(),tagValue);
                }

            }
        });


        stranger_btn.setOnClickListener(view -> {
            if (editText.getEditText().getText().toString().isEmpty())
            {
                Toast.makeText(getApplicationContext(), "enter your name", Toast.LENGTH_SHORT).show();
            }
            else if(tagValue.equals("no"))
            {
                Toast.makeText(getApplicationContext(), "Select Tag", Toast.LENGTH_SHORT).show();
            }
            else
            {
                strangerLogin(editText.getEditText().getText().toString(),tagValue);
            }

        });

        music.setOnClickListener(this::onClick);
        tech.setOnClickListener(this::onClick);
        relationship.setOnClickListener(this::onClick);
        mental_health.setOnClickListener(this::onClick);
        looking_for_advice.setOnClickListener(this::onClick);
        other.setOnClickListener(this::onClick);
    }

    private void attemptLogin( String username,String tag) {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("USERNAME",username);
        intent.putExtra("TAGVALUE",tag);
        startActivity(intent);
    }

    private void strangerLogin( String username,String tag) {
        Intent intent = new Intent(LoginActivity.this,StrangerChat.class);
        intent.putExtra("USERNAME",username);
        intent.putExtra("TAGVALUE",tag);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        MaterialCardView temp_card;
        TextView textView;

        switch (v.getId())
        {
            case R.id.tag_music:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteButton = R.id.tag_music;
                currentHighLiteText = R.id.music_tt;
                setview(music,music_tt);
                tagValue = "Music";
                break;
            case R.id.tag_tech:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteText = R.id.tech_tt;
                currentHighLiteButton = R.id.tag_tech;
                setview(tech,tech_tt);
                tagValue = "Tech";
                break;
            case R.id.tag_relationship:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteText = R.id.relation_tt;
                currentHighLiteButton = R.id.tag_relationship;
                setview(relationship,relationship_tt);
                tagValue = "Relationship";
                break;
            case R.id.tag_mental_health:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteText = R.id.mh_tt;
                currentHighLiteButton = R.id.tag_mental_health;
                setview(mental_health,mental_health_tt);
                tagValue = "Mental Health";
                break;
            case R.id.tag_looking_for_advice:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteText = R.id.loc_tt;
                currentHighLiteButton = R.id.tag_looking_for_advice;
                setview(looking_for_advice,looking_for_advice_tt);
                tagValue = "Looking for advice";
                break;
            case R.id.tag_other:
                temp_card = findViewById(currentHighLiteButton);
                textView = findViewById(currentHighLiteText);
                removeView(temp_card,textView);
                currentHighLiteText = R.id.other_tt;
                currentHighLiteButton = R.id.tag_other;
                setview(other,other_tt);
                tagValue = "Other";
                break;

        }

    }


    void setview(MaterialCardView cardView,TextView textView)
    {
        textView.setTextColor(Color.parseColor("#FFFFFFFF"));
        cardView.setCardBackgroundColor(Color.parseColor("#0099FF"));
    }

    void removeView(MaterialCardView cardView,TextView textView)
    {
        textView.setTextColor(Color.parseColor("#FF000000"));
        cardView.setCardBackgroundColor(Color.parseColor("#FFFFFFFF"));
    }
}