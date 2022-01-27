package com.rsin.socketiochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {
    private EditText editText;
    Button signInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editText =  findViewById(R.id.username_input);
        signInButton =  findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "enter your name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    attemptLogin(editText.getText().toString());
                }

            }
        });
    }

    private void attemptLogin( String username) {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("USERNAME",username);
        startActivity(intent);
    }



}