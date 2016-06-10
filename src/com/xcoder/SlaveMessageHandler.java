package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler implements Runnable{
    private Socket Socket;
    private InputStream In = null;
    private OutputStream Out = null;
    private byte[] Msg = new byte[1024];


    public SlaveMessageHandler(Socket socket) {
        this.Socket = socket;
    }


    public void run() {
        // slave的消息处理线程负责socket通信，视需要选择是否阻塞，一般使用短连接，通信完成就关闭socket
        try {
            In = Socket.getInputStream();
            Out = Socket.getOutputStream();
            int len = In.read(Msg, 0, Msg.length);
            System.out.println("message:" + new String(Msg, 0, len));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }
}
