package com.xcoder;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
    // 存储元数据的列表，存储每个文件的绝对路径和文件类型
    private static Map<String, Integer> MetaTree = new HashMap<String, Integer>();
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
        System.out.println("new slave join:" + slavePort);
    }


    public static synchronized void removeSlave(int slavePort) {
        if (!Server.IsMaster) {
            // 如果不是master角色，不可以操作SlavePorts数据
            return;
        }
        for (int i = 0; i < SlavePorts.size(); i++) {
            int p = SlavePorts.get(i);
            if (p == slavePort) {
                SlavePorts.remove(i);
                System.out.println("remove slave:" + slavePort);
            }
        }

    }


    /**
     * 向MetaTree中添加一个文件
     *
     * @param fileName：文件的绝对路径
     * @param type：文件类型（普通文件或目录）
     * @return 当文件已经存在时返回false
     */
    public static synchronized boolean addMeta(String fileName, int type) {
        for (Map.Entry<String, Integer> entry : MetaTree.entrySet()) {
            if (fileName.equals(entry.getKey())) {
                return false;
            }
        }
        MetaTree.put(fileName, type);
        System.out.println("create file:" + fileName);
        return true;
    }


    /**
     * 检查文件是否存在
     *
     * @param fileName:文件的绝对路径
     * @return 如果存在，返回文件类型；如果不存在，返回-1
     */
    public static synchronized int queryMeta(String fileName) {
        for (Map.Entry<String, Integer> entry : MetaTree.entrySet()) {
            if (fileName.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return -1;
    }


    /**
     * 更新client的工作目录
     *
     * @param id:client  id
     * @param dir:新的工作目录
     */
    public static synchronized void updateWorkingDir(long id, String dir) {
        WorkingDir.put(id, dir);
    }


    /**
     * 查询client的工作目录
     *
     * @param id:client id
     */
    public static synchronized String queryWorkingDir(long id) {
        for (Map.Entry<Long, String> entry : WorkingDir.entrySet()) {
            if (entry.getKey() == id) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * 读取文件信息
     *
     * @param dirName:目录的绝对路径
     * @return 返回文件名和类型（对于目录文件，返回一个列表）
     */
    public static synchronized List<String> readDir(String dirName) {
        List<String> files = new LinkedList<String>();
        for (Map.Entry<String, Integer> entry : MetaTree.entrySet()) {
            String file = entry.getKey();
            if (!file.equals(dirName) && file.contains(dirName)) {
                int len1 = file.split("/").length;
                int len2 = dirName.split("/").length;
                if (dirName.equals("/")) {
                    len2++;
                }

                if (len1 == len2 + 1) {
                    files.add(file);
                }

            }

        }
        return files;
    }


    /**
     * 删除文件
     *
     * @param fileName:文件的绝对路径
     * @return 0:成功
     */
    public static synchronized int removeFile(String fileName) {
        switch (queryMeta(fileName)) {
            case MSG.FILE_COMN:
                MetaTree.remove(fileName);
                System.out.println("remove file:" + fileName);
                break;
            case MSG.FILE_DIR:
                List<String> files = new LinkedList<String>();
                // TODO current modification exception
                for (Map.Entry<String, Integer> entry : MetaTree.entrySet()) {
                    String file = entry.getKey();
                    if (file.contains(fileName)) {
                        files.add(file);
                    }
                }
                for (String file : files) {
                    MetaTree.remove(file);
                    System.out.println("remove file:" + file);
                }
                break;
        }
        return MSG.SYNC_OK;
    }


    /**
     * 删除失去联系的客户端
     */
    public static synchronized void removeClient(long id) {
        for (Map.Entry<Long, String> entry : WorkingDir.entrySet()) {
            if (entry.getKey() == id) {
                WorkingDir.remove(id);
                System.out.println("remove client:" + id);
            }
        }
    }


    /**
     * 与slave同步master上的数据
     *
     * @param port：slave端口
     */
    public static synchronized void syncDataWithSlave(int port) throws IOException {
        /**
         * 以创建文件的方式与slave同步数据
         * 发送消息格式
         * [Head(1B) OpType(1B) ID(8B) FileType(1B) FileName(variable)]
         * 为保证兼容性，ID设置为0
         *
         * 接收消息格式
         * [Head(1B) Status(1B)]
         *
         * */
        byte buf[] = new byte[1024];
        Socket s = new Socket("localhost", port);
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        for (Map.Entry<String, Integer> entry : MetaTree.entrySet()) {
            String fileName = entry.getKey();
            buf[0] = MSG.HEAD_MASTER;
            buf[1] = MSG.MASTER_CREATE_FILE;
            buf[10] = (byte) (int) entry.getValue();
            Util.stringToBytes(fileName, buf, 11);
            out.write(buf, 0, 11 + fileName.length());
            out.flush();
            //等待slave返回ACK，进行下一次迭代
            in.read(buf);
        }
        s.close();
    }


    /**
     * 返回端口列表
     */
    public static synchronized List<Integer> getSlavePorts() {
        return SlavePorts;
    }


}
