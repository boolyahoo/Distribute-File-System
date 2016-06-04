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
    private BufferedReader in;
    private PrintWriter out;



    public ServerMessageHandler(Socket client, long socketID) {
        this.socket = client;
        this.socketID = socketID;
    }


    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String body = null;
            while (true) {
                body = in.readLine();
                if (body == null)
                    break;
                type = body.getBytes()[0];
                byte opType = body.getBytes()[1];
                SocketGroup.addMSocket(socket, socketID, type);
                System.out.println("type : " + type);
                switch (opType){
                    case MSG.CLIENT_OP_DEFULT:
                        //向其他slave转发消息
                        String msg = new String(body.getBytes(), 2, body.getBytes().length - 2);
                        SocketGroup.notifyAllSlave(msg);
                        break;
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
