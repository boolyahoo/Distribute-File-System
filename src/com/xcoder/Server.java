package com.xcoder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by booly on 2016/6/4.
 * Server类保存静态全局静态变量
 * 以及对这些静态变量进行操作的函数
 */

public class Server {
    public static final String HOST = "127.0.0.1";
    public static final int MASTER_PORT = 8080;
    public static int CurPort;
    public static boolean IsMaster;

    private static List<Integer> SlavePorts = new LinkedList<Integer>();
    // 存储元数据的列表，存储每个文件的绝对路径
    private static List<String> MetaTree = new LinkedList<String>();
    // 存储每个Client的当前工作目录
    private static Map<Long, String> WorkingDir = new HashMap<Long, String>();


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


    public static synchronized void addMeta(String fileName, long id){
        for(String file : MetaTree){
            if(file.equals("name"))
                return;
        }
        MetaTree.add(fileName);
    }


    public static synchronized void updateWorkingDir(long id, String dir){
        WorkingDir.put(id, dir);
    }


    public static synchronized String queryWorkingDir(long id){
        for (Map.Entry<Long, String> entry : WorkingDir.entrySet()) {
            if(entry.getKey() == id){
                return entry.getValue();
            }
        }
        return null;
    }


}
