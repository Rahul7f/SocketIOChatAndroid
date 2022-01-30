package com.rsin.socketiochat;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class StrangerChat extends AppCompatActivity {

    private Socket socket;
    private String username;
    String partner_id,partner_username,my_id;
    TextView status, myself,partner_tt;
    String TAG = "STRANGER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_chat);
        status = findViewById(R.id.status);
        myself = findViewById(R.id.t1);
        partner_tt = findViewById(R.id.t2);
        username = getIntent().getStringExtra("USERNAME");
        partner_id = "";
        partner_username = "";



        // connection to server
        try {
            socket = IO.socket("http://192.168.43.93:3003/");
//            socket = IO.socket("https://obscure-badlands-61875.herokuapp.com/");

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Server Down...\nwe are trying to connect...", Toast.LENGTH_SHORT).show();
        }

        socket.connect();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("init", init);
        socket.on("disconnect", onDisconnect);
        socket.off("typing", onTyping);
        socket.off("stop typing", onStopTyping);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on("connect_timeout", onConnectError);
        socket.on("partner", partner);
        socket.on("partner_info", partner_info);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("disconnect", onDisconnect);
        socket.off("typing", onTyping);
        socket.off("stop typing", onStopTyping);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on("connect_timeout", onConnectError);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket.emit("add user", username);
                    status.setText("connected");

                }
            });
            
        }
    };



    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "user disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(), "user typing", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "user typing stopped", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    status.setText("connection error");
                }
            });
        }
    };

    private Emitter.Listener partner = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    // setting
                    if (partner_id=="" || (partner_username.isEmpty() || partner_username.equals("null") ||partner_username.equals("null")))
                    {
                        try {
//                            partner_tt.setText("pp connected socket id:-  "+data.getString("partner_id"));
                            partner_id = data.getString("partner_id");
                            partner_username = data.getString("partner_username");
                            partner_tt.setText("partner: "+partner_username+" "+"socket id:  "+partner_id);

                        } catch (JSONException e) {
                            partner_tt.setText(e.getMessage());
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        JSONObject json = new JSONObject();
                        JSONObject item = new JSONObject();
                        try {
                            item.put("partner_id",socket.id());
                            item.put("partner_username",username);
                            json.put("target",data.get("partner_id"));
                            json.put("data",item);
                            socket.emit("partner",json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You are connected with: "+partner_username, Toast.LENGTH_SHORT).show();
                    }


                }
            });

        }
    };

    private Emitter.Listener partner_info = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    // setting
                    Toast.makeText(getApplicationContext(), "partner info ", Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    private Emitter.Listener init = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    // setting
                    try {
                        myself.setText("myself: "+data.getString("username")+" "+"socket id:  "+data.getString("my_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    };


}