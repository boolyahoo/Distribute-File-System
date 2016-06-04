package com.xcoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by booly on 2016/6/4.
 */

public class SocketGroup {
    private static List<MSocket> socketGroup = new LinkedList<MSocket>();


    public static synchronized void addSocket(Socket socket, long socketID, int type) {
        // 向socketGroup内添加一个socket
        // 如果已经在socketGroup中不再重复添加
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return;
        }
        MSocket msocket = new MSocket(socket, socketID, type);
        socketGroup.add(msocket);

    }

    public static synchronized Socket getSocket(long socketID, int type) {
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return msocket.socket;
        }
        return null;
    }

    public static synchronized void notifyAllSlave(String msg){
        try{
            for (MSocket msocket : socketGroup) {
                if (msocket.type == MSG.HEAD_SLAVE) {
                    Socket s = msocket.socket;
                    PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
                    sout.println(msg);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static synchronized void removeSocket(Socket socket, long socketID, int type){
        for(MSocket msocket : socketGroup){
            if(msocket.socketID == socketID && msocket.type == type){
                socketGroup.remove(msocket);
            }
        }
    }


    public static void closeSocket(Socket socket) {
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
