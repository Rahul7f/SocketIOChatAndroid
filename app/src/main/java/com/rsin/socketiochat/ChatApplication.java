package com.rsin.socketiochat;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication {
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.43.93:3000/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
