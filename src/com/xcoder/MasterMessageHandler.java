package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by xcoder on 2016/4/23.
 */

public class MasterMessageHandler implements Runnable {
    private Socket Socket;
    private long SocketID;
    private InputStream In;
    private OutputStream Out;
    /**
     * 当前线程的消息缓存
     */
    private byte Msg[] = new byte[1024];
    /**
     * 当前线程是否循环执行读取Socket数据的操作
     * master和slave之间的通信用短连接完成，通信结束后断开连接
     * master和client之间的通信使用长链接，master不会主动断开连接，由client断开连接
     */
    private boolean MsgLoopFlag;


    public MasterMessageHandler(Socket socket, long socketID) {
        this.Socket = socket;
        this.SocketID = socketID;
    }


    public void run() {
        try {
            MsgLoopFlag = true;
            In = Socket.getInputStream();
            Out = Socket.getOutputStream();
            while (MsgLoopFlag) {
                int len = In.read(Msg, 0, Msg.length);
                if (len == -1) {
                    return;
                }
                byte type = Msg[0];
                switch (type) {
                    case MSG.HEAD_CLIENT:
                        handleClientMessage(len);
                        break;
                    case MSG.HEAD_SLAVE:
                        handlerSlaveMessage(len);
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


    /**
     * 处理client的消息
     *
     * @param len:接收到的消息字节数
     */
    private void handleClientMessage(int len) throws Exception {
        if (len < 2) {
            System.out.println("bad message");
            return;
        }
        byte opType = Msg[1];
        switch (opType) {
            case MSG.CLIENT_REGISTER:
                handleClientRegister(len);
                break;
            case MSG.CLIENT_DEFAULT:
                //forwardClientMsgToSlave(len);
                break;
            case MSG.CLIENT_QUERY_PWD:
                handleClientWorkingDirQuery(len);
                break;
            case MSG.CLIENT_UPDATE_PWD:
                handleClientWorkingDirUpdate(len);
                break;
        }
    }


    /**
     * 处理slave消息
     *
     * @param len:接收到的消息字节数
     */
    private void handlerSlaveMessage(int len) {
        if (len < 2) {
            System.out.println("bad message body");
            return;
        }
        byte opType = Msg[1];
        switch (opType) {
            case MSG.SLAVE_REGISTER:
                handleSlaveRegister(len);
                break;
            default:
                break;
        }
    }


    private void forwardClientMsgToSlave(int len) {
        Socket s = null;
        OutputStream out = null;
        try {
            // TODO 修改slave端口号
            s = new Socket(Server.HOST, 9000);
            out = s.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(null, out);
            Util.closeSocket(s);
        }
    }


    private void handleClientRegister(int len) throws Exception {
        // 响应client注册请求，将client的socketID返回，作为client在系统中的ID
        byte buf[] = new byte[10];
        buf[0] = MSG.HEAD_MASTER;
        buf[1] = MSG.MASTER_ACK_OK;
        Util.getBytes(SocketID, buf, 2, buf.length);
        // TODO 删除调试输出
        System.out.println("client id : " + SocketID);
        Server.updateWorkingDir(SocketID, "/");
        Out.write(buf, 0, buf.length);
        Out.flush();
    }


    private void handleClientWorkingDirQuery(int len) throws Exception {
        long clientID = Util.parseNum(Msg, 2, 10);
        System.out.println("client ID : " + clientID);
        String dir = Server.queryWorkingDir(clientID);
        if (dir != null) {
            byte buf[] = new byte[2 + dir.length()];
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_ACK_OK;
            for (int i = 0; i < dir.length(); i++) {
                buf[i + 2] = (byte) dir.charAt(i);
            }
            Out.write(buf);
            Out.flush();
        }
    }


    private void handleSlaveRegister(int len) {
        // 获取message的有效信息部分
        int slavePort = (int) Util.parseNum(Msg, 2, 2 + 4);
        Server.addSlavePort(slavePort);
        System.out.println("slave port : " + slavePort);
        // 设置当前线程不再从socket读取数据
        MsgLoopFlag = false;
        // 与slave同步master上的MetaTree
    }


    private void handleClientWorkingDirUpdate(int len) throws Exception {
        /**
         * 将新的工作目录返回给client
         * 接收到消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) TargetDir(variable)]
         * 返回消息格式：
         * [Head(1B) Status(1B) NewWorkingDir(variable)]
         * */
        byte buf[] = new byte[1024];
        buf[0] = MSG.HEAD_MASTER;
        long clientID = Util.parseNum(Msg, 2, 2 + 8);
        String dir = new String(Msg, 10, len - 10);
        String curDir = Server.queryWorkingDir(clientID);
        String parentDir;
        System.out.println("dir : " + dir);
        if (dir.equals("..")) {
            if (curDir.equals("/")) {
                // 已经是根目录不做操作，返回
                buf[1] = MSG.MASTER_ACK_OK;
                for (int i = 0; i < curDir.length(); i++) {
                    buf[i + 2] = (byte) curDir.charAt(i);
                }
                Out.write(buf, 0, 2 + curDir.length());
            } else {
                //找到当前目录的父目录，作为工作目录
                for (int i = curDir.length(); i >= 0; i--) {
                    if (curDir.charAt(i) == '/') {
                        parentDir = curDir.substring(0, i - 1);
                        Server.updateWorkingDir(clientID, parentDir);
                        buf[1] = MSG.MASTER_ACK_OK;
                        for (int j = 0; j < parentDir.length(); j++) {
                            buf[j + 2] = (byte) parentDir.charAt(j);
                        }
                        Out.write(buf, 0, 2 + parentDir.length());
                        break;
                    }
                }
            }
        } else {
            Server.updateWorkingDir(clientID, dir);
            buf[1] = MSG.MASTER_ACK_OK;
            for (int i = 0; i < dir.length(); i++) {
                buf[i + 2] = (byte) dir.charAt(i);
            }
            Out.write(buf, 0, 2 + dir.length());
        }
        Out.flush();
    }

}
