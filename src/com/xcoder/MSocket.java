package com.xcoder;

import java.net.Socket;

/**
 * Created by booly on 2016/6/4.
 */
public class MSocket {
    public Socket socket;
    public long socketID;
    public int type;


    public MSocket(Socket socket, long socketID, int type) {
        this.socket = socket;
        this.socketID = socketID;
        this.type = type;
    }
}
