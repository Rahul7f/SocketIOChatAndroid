package com.rsin.socketiochat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class StrangerChat extends AppCompatActivity {

    private Socket socket;
    private String username;
    String partner_id,partner_username,my_id;
    TextView status;
    String TAG = "STRANGER";
    RecyclerView recyclerView;
    StrangerAdapter strangerAdapter;
    private EditText messageInputBox;
    private List<StrangeMessage> messageList = new ArrayList<StrangeMessage>();
    RelativeLayout send;
    private Handler mTypingHandler = new Handler();
    private boolean mTyping = false;
    private static final int TYPING_TIMER_LENGTH = 600;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_chat);
        status = findViewById(R.id.status);
        messageInputBox = findViewById(R.id.et);
        recyclerView = findViewById(R.id.recyclerview);
        send = findViewById(R.id.send_ret);

        username = getIntent().getStringExtra("USERNAME");
        partner_id = "";
        partner_username = "";

        strangerAdapter = new StrangerAdapter(messageList,getApplicationContext());
        recyclerView.setAdapter(strangerAdapter);



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
        socket.on("disconnecting now", disconnecting_now);
        socket.on("chat message partner", chat_message_partner);
        socket.on("chat message mine", chat_message_mine);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        messageInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        messageInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == username) return;
                if (!socket.connected()) return;

                if (!mTyping) {
                    mTyping = true;

                    try {
                        JSONObject data = new JSONObject();
                        data.put("target",partner_id);
                        data.put("sourceUsername",username);
                        socket.emit("typing",data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;
            mTyping = false;
            try {
                JSONObject data = new JSONObject();
                data.put("target",partner_id);
                data.put("sourceUsername",username);
                socket.emit("stop typing",data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

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
                    status.setText("finding match...");
                    status.setBackgroundColor(Color.parseColor("#e21400"));
                    status.setTextColor(Color.parseColor("#FFFFFFFF"));
                }
            });
            
        }
    };

    private Emitter.Listener chat_message_mine = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String msg =  data.getString("u_msg");
                        String username =  data.getString("u_user");
                        StrangeMessage strangeMessage = new StrangeMessage(username,msg,"IN");
                        messageList.add(strangeMessage);
                        strangerAdapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    };

    private Emitter.Listener chat_message_partner = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String msg =  data.getString("u_msg");
                        String username =  data.getString("u_user");
                        StrangeMessage strangeMessage = new StrangeMessage(username,msg,"OUT");
                        messageList.add(strangeMessage);
                        strangerAdapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    };


    private Emitter.Listener disconnecting_now = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "partner gone", Toast.LENGTH_SHORT).show();
                    finish();
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
                    boolean b = (boolean) args[0];
                    if (!b)
                    {
                        Toast.makeText(getApplicationContext(), "user typing", Toast.LENGTH_LONG).show();
                    }

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
                    boolean b = (boolean) args[0];
                    if (b)
                    {
                        Toast.makeText(getApplicationContext(), "user typing", Toast.LENGTH_LONG).show();
                    }
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
                    status.setBackgroundColor(Color.parseColor("#e21400"));
                    status.setTextColor(Color.parseColor("#FFFFFFFF"));
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
                            partner_id = data.getString("partner_id");
                            partner_username = data.getString("partner_username");
                            status.setText("You are connected with: "+partner_username);
                            status.setBackgroundColor(Color.parseColor("#3b88eb"));
                            status.setTextColor(Color.parseColor("#FFFFFFFF"));
                        } catch (JSONException e) {
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
                        my_id = data.getString("my_id");
                        username = data.getString("username");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    };


    private void attemptSend() {
        if (null == username){
            Toast.makeText(getApplicationContext(), "user name is null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!socket.connected()){
            Toast.makeText(getApplicationContext(), "Socket not not connected\nwe are trying to connect...", Toast.LENGTH_SHORT).show();
            return;
        }
//        mTyping = false;

        String message = messageInputBox.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter some text", Toast.LENGTH_SHORT).show();
            messageInputBox.requestFocus();
            return;
        }
        messageInputBox.setText("");
//        addMessage(username, message);

        // perform the sending message attempt.

        try {
            JSONObject msgObj = new JSONObject();
            msgObj.put("msg",message);
            msgObj.put("target",partner_id);
            msgObj.put("target",partner_id);
            msgObj.put("sourceUsername",username);
            socket.emit("chat message", msgObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void scrollToBottom() {
        recyclerView.scrollToPosition(strangerAdapter.getItemCount() - 1);
    }


}

//msg: msg, target: partner_id