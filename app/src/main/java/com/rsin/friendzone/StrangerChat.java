package com.rsin.friendzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

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
    TextView status,typing_status;
//    ImageView end_chat;
    String tagValue;
    RecyclerView recyclerView;
    StrangerAdapter strangerAdapter;
    private EditText messageInputBox;
    private List<StrangeMessage> messageList = new ArrayList<StrangeMessage>();
    ImageView send;
    private Handler mTypingHandler = new Handler();
    private boolean mTyping = false;
    private static final int TYPING_TIMER_LENGTH = 600;
    ConstraintLayout input_message_layout;
    View appbar;
    ImageView refresh_stranger;
    TextView partner_tag,partner_name;
    LinearLayout connection_color_status;
    TextView connection_status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_chat);
        status = findViewById(R.id.patner_status);
        typing_status = findViewById(R.id.typing_status);
        partner_name = findViewById(R.id.partner_name);
        connection_status = findViewById(R.id.connection_status);
        connection_color_status = findViewById(R.id.connection_color_status);
//        end_chat = findViewById(R.id.chat_end_button);
        messageInputBox = findViewById(R.id.message_et);
        recyclerView = findViewById(R.id.recyclerview);
        send = findViewById(R.id.sent_button_stranger);
        input_message_layout = findViewById(R.id.input_message_layout);
        appbar = findViewById(R.id.stranger_topbar);
        refresh_stranger = findViewById(R.id.refresh_stranger);
        partner_tag = findViewById(R.id.partner_tag_tt);

        username = getIntent().getStringExtra("USERNAME");
        tagValue = getIntent().getStringExtra("TAGVALUE");
        partner_id = "";
        partner_username = "";

        strangerAdapter = new StrangerAdapter(messageList,getApplicationContext());
        recyclerView.setAdapter(strangerAdapter);



        // connection to server
        try {
//            socket = IO.socket("http://192.168.43.93:3003/");
            socket = IO.socket("https://stranger-chat-server.herokuapp.com/");

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Server Down...\nwe are trying to connect...", Toast.LENGTH_SHORT).show();
        }

        socket.connect();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("init", init);
        socket.on("disconnect", onDisconnect);
        socket.on("typingIND", onTyping);
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
                if (id == R.id.sent_button_stranger || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        refresh_stranger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActivity();
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
                        if (!partner_id.isEmpty() && !partner_username.isEmpty())
                        {
                            data.put("target",partner_id);
                            data.put("sourceUsername",username);
                            socket.emit("typing",data);
                            Log.d("error",username+" "+partner_id);
                            Log.d("error","typing requested");
                        }else {
                            Log.d("error","variable are empty");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error",e.getMessage());
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
                    status.setTextColor(Color.parseColor("#e21400"));
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
                    refreshActivity();
//                    Toast.makeText(getApplicationContext(), "User gone", Toast.LENGTH_SHORT).show();
//                    finish();
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
//                    Toast.makeText(getApplicationContext(), "user disconnected", Toast.LENGTH_LONG).show();
                    //TODO  reload activity
                    status.setVisibility(View.VISIBLE);
                    status.setText("disconnect");
                    connection_color_status.setBackgroundColor(Color.parseColor("#e21400"));
                    connection_status.setText("Disconnect");
                    recyclerView.setVisibility(View.GONE);
                    appbar.setVisibility(View.GONE);
                    input_message_layout.setVisibility(View.GONE);

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
                        typing_status.setText("Typing...");
                    }
                    else {
                        typing_status.setText("");
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
                    status.setTextColor(Color.parseColor("#e21400"));
                    connection_color_status.setBackgroundColor(Color.parseColor("#e21400"));
                    connection_status.setText("connection error");

                    status.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    appbar.setVisibility(View.GONE);
                    input_message_layout.setVisibility(View.GONE);


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
                            status.setText(partner_username.trim()+" connected");
                            status.setTextColor(Color.parseColor("#3b88eb"));
                            if (!partner_username.isEmpty() && partner_username!=null && partner_username !="null")
                            {
                                partner_tag.setText(data.getString("tag"));
                                partner_name.setText(partner_username);
                                connection_color_status.setBackgroundColor(Color.parseColor("#58dc00"));
                                connection_status.setText("Connected");
                            }

                            status.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            appbar.setVisibility(View.VISIBLE);
                            input_message_layout.setVisibility(View.VISIBLE);

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        JSONObject json = new JSONObject();
                        JSONObject item = new JSONObject();
                        try {
                            item.put("partner_id",socket.id());
                            item.put("partner_username",username);
                            item.put("tag",tagValue);
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
            return;
        }
        if (!socket.connected()){
            connection_color_status.setBackgroundColor(Color.parseColor("#e21400"));
            connection_status.setText("we are trying to connect...");
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
    
    void refreshActivity()
    {
        finish();
        startActivity(getIntent());
    }

}

//msg: msg, target: partner_id