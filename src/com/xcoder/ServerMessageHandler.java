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
    private Socket socket;
    private long socketID;
    private int type;
    private BufferedReader sin;
    private PrintWriter sout;


    public ServerMessageHandler(Socket socket, long socketID) {
        this.socket = socket;
        this.socketID = socketID;
    }


    public void run() {
        try {
            sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sout = new PrintWriter(socket.getOutputStream(), true);
            String body = null;
            while (true) {
                // 对socket的读会导致线程挂起
                body = sin.readLine();
                if (body == null) {
                    break;
                }
                System.out.println("message:" + body);
                type = body.getBytes()[0];
                // 分发消息
                switch (type) {
                    case MSG.HEAD_CLIENT:
                        handleClient(body);
                        break;
                    case MSG.HEAD_SLAVE:
                        handlerSlave(body);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            Util.closeStream(sin, sout);
            // 从SocketGroup中删除当前socket
            SocketGroup.removeSocket(socket, socketID, type);
            SocketGroup.closeSocket(socket);
        }
    }

    private void handleClient(String body) {
        if (body.length() < 2) {
            System.out.println("bad message body");
            return;
        }
        byte opType = body.getBytes()[1];
        switch (opType) {
            case MSG.CLIENT_REGISTER:
                SocketGroup.addSocket(socket, socketID, type);
                break;
            case MSG.CLIENT_DEAFULT:
                String msg = new String(body.getBytes(), 2, body.getBytes().length - 2);
                SocketGroup.notifyAllSlave(msg);
                break;
            case MSG.CLIENT_QUERY_PWD:
                sout.println("/home/xcoder");
                break;
        }
    }

    private void handlerSlave(String body) {
        if (body.length() < 2) {
            System.out.println("bad message body");
            return;
        }
        byte opType = body.getBytes()[1];
        switch (opType) {
            case MSG.SLAVE_REGISTER:
                SocketGroup.addSocket(socket, socketID, type);
                break;
            default:
                break;

        }
    }
}
