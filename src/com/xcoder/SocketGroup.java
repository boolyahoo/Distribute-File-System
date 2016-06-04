package com.xcoder;

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
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return;
        }
        MSocket msocket = new MSocket(socket, socketID, type);
        socketGroup.add(msocket);
    }

    public static synchronized MSocket getMsocket(long socketID, int type){
        for (MSocket msocket : socketGroup) {
            if (msocket.socketID == socketID && msocket.type == type)
                return msocket;
        }
        return null;
    }



}
