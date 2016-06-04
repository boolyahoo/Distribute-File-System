package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler {
    private Socket socket;
    private BufferedReader sin = null;
    private PrintWriter sout = null;


    public SlaveMessageHandler(Socket socket) {
        this.socket = socket;
    }


    public void run() {
        try {
            sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sout = new PrintWriter(socket.getOutputStream(), true);
            // 向master发送消息表明自己是slave
            byte message[] = {MSG.HEAD_SLAVE, MSG.SLAVE_REGISTER};
            sout.println(new String(message));
            while (true) {
                // 从socket读数据，如果没有数据，当前线程会被挂起
                String msg = sin.readLine();
                System.out.println("server:" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            Util.closeStream(sin, sout);
            // 关闭socket
            SocketGroup.closeSocket(socket);
        }

    }
}
