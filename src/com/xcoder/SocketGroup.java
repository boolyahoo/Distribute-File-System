package com.xcoder;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by booly on 2016/6/4.
 */

public class SocketGroup {
    private static List<MSocket> socketGroup = new LinkedList<MSocket>();


    public static synchronized void addMSocket(Socket socket, long socketID, int type) {
        // 向socketGroup内添加一个socket
        // 如果已经在socketGroup中不再重复添加
        System.out.println("add socket,socketID:" + socketID + ", type" + type);
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return;
        }
        MSocket msocket = new MSocket(socket, socketID, type);
        socketGroup.add(msocket);
        System.out.println("add socket,socketGroup size :" + socketGroup.size());

    }

    public static synchronized Socket getMsocket(long socketID, int type){
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return msocket.socket;
        }
        return null;
    }

    public static synchronized void notifyAllSlave(String msg) throws Exception{
        System.out.println("notify all slave");
        for(MSocket msocket : socketGroup){
            if(msocket.type == MSG.HEAD_SLAVE){
                Socket s = msocket.socket;
                PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
                sout.println(msg);
                System.out.println("notyfy all slave: one");
            }
        }
    }



}
