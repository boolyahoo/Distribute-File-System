package com.xcoder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

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
            // 出现异常，移除client
            Server.removeClient(SocketID);
        } finally {
            // 出现异常，移除client
            Server.removeClient(SocketID);
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }


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
            case MSG.CLIENT_CREATE_FILE:
                handleClientCreateFile(len);
                break;
            case MSG.CLIENT_READ_FILE:
                handleClientReadFile(len);
                break;
            case MSG.CLIENT_DELETE_FILE:
                handleClientDeleteFile(len);
                break;
        }
    }


    private void handlerSlaveMessage(int len) throws Exception{
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


    private void forwardClientMsgToSlaves(int len) {
        Socket s = null;
        OutputStream out = null;
        try {
            // TODO 修改slave端口号
            s = new Socket("localhost", 9000);
            out = s.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(null, out);
            Util.closeSocket(s);
        }
    }


    private void handleClientRegister(int len) throws Exception {
        /**
         * 接收消息格式：
         * [Head(1B) OpType(1B)]
         * 返回消息格式：
         * [Head(1B) Status(1B) ClientID(8B)]
         * */
        System.out.println("new client:" + SocketID);
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
        /**
         * 接收到消息格式：
         * [Head(1B) OpType(1B) ClientID(8B)]
         * 返回消息格式：
         * [Head(1B) Status(1B) WorkingDir|Message(variable)]
         * */
        long clientID = Util.parseNum(Msg, 2, 10);
        byte buf[] = new byte[1024];
        String curDir = Server.queryWorkingDir(clientID);
        if (curDir != null) {
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_ACK_OK;
            Util.stringToBytes(curDir, buf, 2);
            Out.write(buf, 0, 2 + curDir.length());
        } else {
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_ACK_FAIL;
            String msg = "error";
            Util.stringToBytes(msg, buf, 2);
            Out.write(buf, 0, 2 + msg.length());
        }
        Out.flush();
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
        String targetDir = new String(Msg, 10, len - 10);
        String curDir = Server.queryWorkingDir(clientID);
        String parentDir;
        System.out.println("dir : " + targetDir);
        if (targetDir.equals("..")) {
            if (curDir.equals("/")) {
                // 已经是根目录不做操作，返回
                buf[1] = MSG.MASTER_ACK_OK;
                Util.stringToBytes(curDir, buf, 2);
                Out.write(buf, 0, 2 + curDir.length());
            } else {
                //找到当前目录的父目录，作为工作目录
                for (int i = curDir.length() - 1; i >= 0; i--) {
                    if (curDir.charAt(i) == '/') {
                        parentDir = curDir.substring(0, i + 1);
                        Server.updateWorkingDir(clientID, parentDir);
                        buf[1] = MSG.MASTER_ACK_OK;
                        Util.stringToBytes(parentDir, buf, 2);
                        Out.write(buf, 0, 2 + parentDir.length());
                        break;
                    }
                }
            }
        } else {
            if (targetDir.charAt(0) == '/') {
                // 目标目录为绝对路径表示，检查目录是否存在
                if (Server.queryMeta(targetDir) == MSG.FILE_DIR) {
                    //如果存在，则更新工作目录，返回当前工作目录
                    Server.updateWorkingDir(clientID, targetDir);
                    buf[1] = MSG.MASTER_ACK_OK;
                    Util.stringToBytes(targetDir, buf, 2);
                    Out.write(buf, 0, 2 + targetDir.length());
                } else {
                    buf[1] = MSG.MASTER_ACK_FAIL;
                    String msg = "error:target directory does not exist";
                    Util.stringToBytes(msg, buf, 2);
                    Out.write(buf, 0, 2 + msg.length());
                }
            } else {
                //目标目录为当前工作目录下的目录
                if (curDir.equals("/")) {
                    targetDir = curDir + targetDir;
                } else {
                    targetDir = curDir + "/" + targetDir;
                }
                if (Server.queryMeta(targetDir) == MSG.FILE_DIR) {
                    //目标目录存在
                    Server.updateWorkingDir(clientID, targetDir);
                    buf[1] = MSG.MASTER_ACK_OK;
                    Util.stringToBytes(targetDir, buf, 2);
                    Out.write(buf, 0, 2 + targetDir.length());
                } else {
                    buf[1] = MSG.MASTER_ACK_FAIL;
                    String msg = "target directory does not exists or not a directory";
                    Util.stringToBytes(msg, buf, 2);
                    Out.write(buf, 0, 2 + msg.length());
                }

            }
        }
        Out.flush();
    }


    private void handleClientCreateFile(int len) throws Exception {
        /**
         * 在当前工作目录下创建一个文件（目录）
         *
         * 接收消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileType(1B) DirName(variable)]
         * 返回的消息格式：
         * [Head(1B) Status(1B) Message(variable)]
         * */
        byte buf[] = new byte[128];
        long clientID = Util.parseNum(Msg, 2, 2 + 8);
        byte fileType = Msg[10];
        String fileName = new String(Msg, 11, len - 11);
        String curDir = Server.queryWorkingDir(clientID);
        if (curDir != null) {
            if (curDir.equals("/")) {
                fileName = curDir + fileName;
            } else {
                fileName = curDir + "/" + fileName;
            }
        }
        System.out.println("create file name:" + fileName);
        if (Server.addMeta(fileName, fileType)) {
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_ACK_OK;
            Out.write(buf, 0, 2);
        } else {
            // 失败的情形为文件已经存在
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_ACK_FAIL;
            String msg = "error:file already exists";
            Util.stringToBytes(msg, buf, 2);
            Out.write(buf, 0, 2 + msg.length());
        }
        Out.flush();

    }


    private void handleClientReadFile(int len) throws Exception {
        /**
         * 读取文件内容
         *
         * 接收消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileName(variable)]
         *
         * 返回的消息格式：
         * OK : [Head(1B) Status(1B) FileList]
         * FileList:
         * Type(1B) FileNameLength(1B) FileName(variable)
         * FAIL : [Head(1B) Status(1B) ErrorMessage(variable)]
         * */
        byte buf[] = new byte[1024];
        buf[0] = MSG.HEAD_MASTER;
        long clientID = Util.parseNum(Msg, 2, 2 + 8);
        // file为绝对路径表示的文件
        String fileName = new String(Msg, 10, len - 10);
        switch (Server.queryMeta(fileName)) {
            case MSG.FILE_COMN:
                buf[1] = MSG.MASTER_ACK_OK;
                Out.write(buf, 0, 2);
                break;
            case MSG.FILE_DIR:
                buf[1] = MSG.MASTER_ACK_OK;
                List<String> files = Server.readDir(fileName);
                int index = 2;
                for (String f : files) {
                    // Type
                    buf[index++] = (byte) Server.queryMeta(f);
                    // FileNameLength
                    buf[index++] = (byte) f.length();
                    // FileName
                    Util.stringToBytes(f, buf, index);
                    index += f.length();
                }
                Out.write(buf, 0, index);
                break;
            default:
                break;
        }
        Out.flush();
    }


    private void handleClientDeleteFile(int len) throws Exception {
        /**
         * 删除文件
         *
         * 接收消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileName(variable)]
         *
         * 返回的消息格式：
         * [Head(1B) Status(1B) Message(variable)]
         * */
        byte buf[] = new byte[1024];
        buf[0] = MSG.HEAD_MASTER;
        long clientID = Util.parseNum(Msg, 2, 2 + 8);
        String curDir = Server.queryWorkingDir(clientID);
        String fileName = new String(Msg, 10, len - 10);
        switch (Server.removeFile(fileName)) {
            case MSG.SYNC_OK:
                buf[1] = MSG.MASTER_ACK_OK;
                break;
            case MSG.SYNC_OCCUPIED:
                break;
            case MSG.SYNC_NOT_EXIST:
                break;
        }
        Out.write(buf, 0, 2);
        Out.flush();
    }


    private void handleSlaveRegister(int len) throws Exception {
        /**
         * 接收消息格式：
         * [Head(1B) OpType(1B) SlavePort(4B)]
         *
         * 发送消息格式：
         * [Head(1B) Status(1B)]
         *
         * */
        byte buf[] = new byte[1024];
        int slavePort = (int) Util.parseNum(Msg, 2, 2 + 4);
        Server.addSlavePort(slavePort);
        System.out.println("slave port : " + slavePort);
        // 设置当前线程不再从socket读取数据
        MsgLoopFlag = false;
        buf[0] = MSG.HEAD_MASTER;
        buf[1] = MSG.MASTER_ACK_OK;
        Out.write(buf, 0, 2);
        try {
            Out.close();
        } catch (Exception e) {
            // 遇到异常，关闭与slave的链接
            Server.removeSlavePort(slavePort);
        } finally {
            // 在这里与slave同步数据
            try{
                Server.syncDataWithSlave(slavePort);
            }catch (Exception e){
                // 出现异常
                e.printStackTrace();
            }


        }
    }


    private void syncDataWithSlave() throws Exception{

    }


}
