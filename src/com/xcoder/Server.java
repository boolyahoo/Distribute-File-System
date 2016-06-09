package com.xcoder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by booly on 2016/6/4.
 * Server保存静态全局静态变量
 * 以及对这些静态变量进行操作的函数
 */

public class Server {
    public static final String HOST = "127.0.0.1";
    public static final int MASTER_PORT = 8080;
    public static int CurPort;
    public static boolean IsMaster;

    private static List<Integer> SlavePorts = new LinkedList<Integer>();
    //存储元数据的表
    private static Map<String, Integer> MetaTree;


    public static synchronized void addSlavePort(int slavePort) {
        if (!Server.IsMaster) {
            // 如果不是master角色，不可以操作SlavePorts数据
            return;
        }
        for (int p : SlavePorts) {
            if (p == slavePort)
                return;
        }
        SlavePorts.add(slavePort);
    }

}
