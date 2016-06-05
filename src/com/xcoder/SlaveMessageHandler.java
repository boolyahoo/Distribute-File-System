package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler implements Runnable{
    private Socket Socket;
    private BufferedReader In = null;
    private PrintWriter Out = null;


    public SlaveMessageHandler(Socket socket) {
        this.Socket = socket;
    }


    public void run() {
        // slave的消息处理线程负责socket通信，视需要选择是否阻塞，一般使用短连接，通信完成就关闭socket
        try {
            In = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
            Out = new PrintWriter(Socket.getOutputStream(), true);
            String msg = In.readLine();
            System.out.println("message:" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }
}
