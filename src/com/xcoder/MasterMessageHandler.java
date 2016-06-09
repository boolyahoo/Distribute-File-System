package com.xcoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by xcoder on 2016/4/23.
 */

public class MasterMessageHandler implements Runnable {
    private Socket Socket;
    private long SocketID;
    private BufferedReader In;
    private PrintWriter Out;
    /**
     * 当前线程是否循环执行读取Socket数据的操作
     * master和slave之间的通信用短连接完成，通信结束后断开连接
     * master和client之间的通信使用长链接，master不会主动断开连接，由client断开连接
     * */
    private boolean MsgLoopFlag;


    public MasterMessageHandler(Socket socket, long socketID) {
        this.Socket = socket;
        this.SocketID = socketID;
    }


    public void run() {
        try {
            MsgLoopFlag = true;
            In = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
            Out = new PrintWriter(Socket.getOutputStream(), true);
            while (MsgLoopFlag) {
                byte msg[] = In.readLine().getBytes();
                if (msg == null) {
                    return;
                }
                System.out.println("message:" + msg);
                byte type = msg[0];
                // 分发消息
                switch (type) {
                    case MSG.HEAD_CLIENT:
                        handleClientMessage(msg);
                        break;
                    case MSG.HEAD_SLAVE:
                        handlerSlaveMessage(msg);
                        break;
                }
            }
            // TODO 删除调试输出
            System.out.println("thread closing...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }

    private void handleClientMessage(byte msg[]) throws Exception {
        if (msg.length < 2) {
            System.out.println("bad message body");
            return;
        }
        byte opType = msg[1];
        switch (opType) {
            case MSG.CLIENT_REGISTER:
                // 响应client注册请求，将client的socketID返回，作为client在系统中的ID
                byte head[] = {MSG.HEAD_MASTER, MSG.MASTER_ACK};
                byte buf[] = new byte[8];
                Util.getBytes(SocketID, buf, 0);
                // TODO 删除调试输出
                System.out.println("client id : " + SocketID );
                Out.println(new String(head) + new String(buf));
                break;
            case MSG.CLIENT_DEFAULT:
                forwardClientMsgToSlave(msg);
                break;
            case MSG.CLIENT_QUERY_PWD:

                Out.println("/home/xcoder");
                break;
        }
    }

    private void handlerSlaveMessage(byte msg[]) {
        if (msg.length < 2) {
            System.out.println("bad message body");
            return;
        }
        byte opType = msg[1];
        switch (opType) {
            case MSG.SLAVE_REGISTER:
                // 获取message的有效信息部分
                int slavePort = (int)Util.parseNum(msg, 2, msg.length);
                Server.addSlavePort(slavePort);
                System.out.println("slave port : " + slavePort);
                Out.println("register successfully");
                // 设置当前线程不再从socket读取数据
                MsgLoopFlag = false;
                break;
            default:
                break;
        }
    }


    private void forwardClientMsgToSlave(byte msg[]) {
        String data = new String(msg, 2, msg.length - 2);
        Socket s = null;
        PrintWriter out = null;
        try {
            // TODO 修改slave端口号
            s = new Socket(Server.HOST, 9000);
            out = new PrintWriter(s.getOutputStream(), true);
            out.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(null, out);
            Util.closeSocket(s);
        }

    }
}
