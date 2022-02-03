package com.rsin.socketiochat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

public class MainActivity extends AppCompatActivity {
    ImageView send;
    private Socket socket;
    TextView status;
    private List<Message> messageList = new ArrayList<Message>();

    private RecyclerView recyclerView;
    private EditText messageInputBox;

    private static final String TAG = "MainFragment";
    private Boolean isConnected = true;

    private static final int REQUEST_LOGIN = 0;

    private static final int TYPING_TIMER_LENGTH = 600;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String username;
    private RecyclerView.Adapter adapter;
    TextView total_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageInputBox = findViewById(R.id.message_et);
        recyclerView = findViewById(R.id.recyclerview);
        status = findViewById(R.id.status);
        total_user = findViewById(R.id.active_user);
        adapter = new MessageAdapter(getApplicationContext(), messageList);
        recyclerView.setAdapter(adapter);
        send = findViewById(R.id.sent_button_chatroom);
        username = getIntent().getStringExtra("USERNAME")+":";



        try {
//            socket = IO.socket("http://192.168.43.93:3000/");
            socket = IO.socket("https://obscure-badlands-61875.herokuapp.com/");

            status.setBackgroundColor(Color.parseColor("#e21400"));
            status.setTextColor(Color.parseColor("#FFFFFFFF"));
            status.setText("Connecting.....");


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Server Down...\nwe are trying to connect...", Toast.LENGTH_SHORT).show();
        }

        socket.connect();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on("connect_timeout", onConnectError);
        socket.on("new message", onNewMessage);
        socket.on("user joined", onUserJoined);
        socket.on("user left", onUserLeft);
        socket.on("typing", onTyping);
        socket.on("stop typing", onStopTyping);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        messageInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.sent_button_chatroom || id == EditorInfo.IME_NULL) {
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
                    socket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            finish();
            return;
        }
        username = data.getStringExtra("username");
        int numUsers = data.getIntExtra("numUsers", 1);

        addLog(getResources().getString(R.string.message_welcome));
        addParticipantsLog(numUsers);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off("connect_timeout", onConnectError);
        socket.off("new message", onNewMessage);
        socket.off("user joined", onUserJoined);
        socket.off("user left", onUserLeft);
        socket.off("typing", onTyping);
        socket.off("stop typing", onStopTyping);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();
                    status.setBackgroundColor(Color.parseColor("#58dc00"));
                    status.setTextColor(Color.parseColor("#FFFFFFFF"));
                    status.setText("Connected");

                    socket.emit("add user", username);

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
                    status.setBackgroundColor(Color.parseColor("#e21400"));
                    status.setText("Disconnected");
                    status.setTextColor(Color.parseColor("#FFFFFFFF"));
                    Log.i(TAG, "disconnected");
                    isConnected = false;
                    Toast.makeText(getApplicationContext(), R.string.disconnect, Toast.LENGTH_LONG).show();
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
                    status.setBackgroundColor(Color.parseColor("#e21400"));
                    status.setText("Connecting.....");
                    status.setTextColor(Color.parseColor("#FFFFFFFF"));
                    Log.e(TAG, "Error connecting");
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    removeTyping(username);
                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                    removeTyping(username);
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
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addTyping(username);
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
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            socket.emit("stop typing");
        }
    };

    private void scrollToBottom() {
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void addLog(String message) {
        messageList.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
        total_user.setText("User Currently Active : "+numUsers);
    }

    private void addMessage(String username, String message) {
        messageList.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        messageList.add(new Message.Builder(Message.TYPE_ACTION)
                .message("Typing...")
                .username(username).build());
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                messageList.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == username){
            Toast.makeText(getApplicationContext(), "user name is null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!socket.connected()){
            Toast.makeText(getApplicationContext(), "Socket not not connected\nwe are trying to connect...", Toast.LENGTH_SHORT).show();
            return;
        }
        mTyping = false;

        String message = messageInputBox.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter some text", Toast.LENGTH_SHORT).show();
            messageInputBox.requestFocus();
            return;
        }
        messageInputBox.setText("");
        addMessage(username, message);

        // perform the sending message attempt.
        socket.emit("new message", message);
    }

    
}