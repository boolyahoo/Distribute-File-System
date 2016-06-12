package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler implements Runnable {
    private Socket Socket;
    private InputStream In = null;
    private OutputStream Out = null;
    private byte[] Msg = new byte[1024];
    private boolean MsgLoopFlag;


    public SlaveMessageHandler(Socket socket) {
        this.Socket = socket;
    }


    public void run() {
        // slave的消息处理线程负责socket通信，视需要选择是否阻塞，一般使用短连接，通信完成就关闭socket
        try {
            MsgLoopFlag = true;
            In = Socket.getInputStream();
            Out = Socket.getOutputStream();
            while (MsgLoopFlag) {
                int len = In.read(Msg, 0, Msg.length);
                if (len == -1) {
                    return;
                }
                byte opType = Msg[1];
                switch (opType) {
                    case MSG.MASTER_CREATE_FILE:
                        handleMasterCreateFile(len);
                        break;
                    case MSG.MASTER_DELETE_FILE:
                        handleMasterDeleteFile(len);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }


    private void handleMasterCreateFile(int len) throws Exception {
        /**
         * 接收消息格式
         * [Head(1B) OpType(1B) ID(8B) FileType(1B) FileName(variable)]
         * 文件名为绝对路径表示,ID为空
         *
         * 返回消息格式
         * [Head(1B) Status(1B)]
         *
         * */
        byte buf[] = new byte[1024];
        byte opType = Msg[1];
        byte fileType = Msg[10];
        String fileName = new String(Msg, 11, len - 11);
        Server.addMeta(fileName, fileType);
        buf[0] = MSG.HEAD_SLAVE;
        buf[1] = MSG.SLAVE_ACK_OK;
        Out.write(buf, 0, 2);
        Out.flush();
        //由发起连接的一方负责关闭连接
    }


    private void handleMasterDeleteFile(int len) throws Exception {
        /**
         * 接收消息格式：
         *
         * [Head(1B) OpType(1B) ID(8B) FileName(variable)]
         * 文件名为绝对路径表示,ID为空
         *
         * */
        byte buf[] = new byte[1024];
        byte opType = Msg[1];
        String fileName = new String(Msg, 10, len - 10);
        Server.removeFile(fileName);
        buf[0] = MSG.HEAD_SLAVE;
        buf[1] = MSG.SLAVE_ACK_OK;
        Out.write(buf, 0, 2);
        Out.flush();
        //由发起连接的一方负责关闭连接
    }


}
