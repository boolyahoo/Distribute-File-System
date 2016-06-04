package com.xcoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by xcoder on 2016/4/23.
 */

public class ServerMessageHandler implements Runnable {
    private Socket socket = null;
    private long socketID = 0;
    private int type;



    public ServerMessageHandler(Socket client, long socketID) {
        this.socket = client;
        this.socketID = socketID;
    }


    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        System.out.println("socket address : " + socket.getLocalPort());
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(this.socket.getOutputStream(), true);

            String body = null;
            while (true) {
                body = in.readLine();
                if (body == null)
                    break;
                byte type = body.getBytes()[0];
                byte opType = body.getBytes()[1];
                if(type == MSG.HEAD_CLIENT){
                    this.type = MSG.HEAD_CLIENT;
                    SocketGroup.addMSocket(socket, socketID, MSG.HEAD_CLIENT);
                }
                if(SocketGroup.getMsocket(socketID, MSG.HEAD_CLIENT) != null){
                    System.out.println("add success");
                }
                switch (opType){
                    case MSG.CLIENT_QUERY_PWD:
                        out.println("/home/xcoder");
                        break;
                }
            }
        } catch (Exception e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                socket = null;
            }
        }
    }
}
