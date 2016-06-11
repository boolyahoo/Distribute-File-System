/**
 * Copyright : xcoder boolyahoo@gmail.com
 */

package com.xcoder;

import org.apache.commons.cli.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Boot用于启动当前Server，对Server进行基本配置
 * 包括server的角色（master or slave）
 * server监听的端口号（由命令行配置输入）
 */
public class Boot {


    public static void main(String[] args) throws Exception {
        new Boot().run(args);
    }


    public void run(String args[]) throws Exception {
        parseArgs(args);
        if (Server.IsMaster) {
            initAsMaster();
        } else {
            initAsSlave();
        }
    }


    private void initAsMaster() throws Exception {
        System.out.println("current node : master");
        System.out.println("port : " + Server.CurPort);
        ServerSocket server = null;
        // Master初始化向MetaTree中添加一条文件目录起始点
        Server.addMeta("/", MSG.FILE_DIR);
        try {
            server = new ServerSocket(Server.CurPort);
            while (true) {
                // 有新的连接进入，新建一个线程处理
                Socket client = server.accept();
                new Thread(new MasterMessageHandler(client, System.currentTimeMillis())).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.close();
                server = null;
            }
        }
    }


    private void initAsSlave() throws Exception {
        System.out.println("current node : slave");
        System.out.println("port : " + Server.CurPort);
        ServerSocket slave = null;
        // slave初始化向MetaTree中添加一条文件目录起始点
        Server.addMeta("/", MSG.FILE_DIR);
        try {
            // 将slave初始化在9000监听
            slave = new ServerSocket(Server.CurPort);
            registerSlave();
            while (true) {
                // 有新的连接进入，新建一个线程处理
                Socket client = slave.accept();
                new Thread(new SlaveMessageHandler(client)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (slave != null) {
                slave.close();
                slave = null;
            }
        }
    }


    private void registerSlave() throws Exception{
        /**
         * 新的slave进程向master注册自己并同步master数据
         *
         * 发送消息格式：
         * [Head(1B) OpType(1B) SlavePort(4B)]
         *
         * 接收消息格式：
         * [Head(1B) Status(1B)]
         *
         * */
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        byte buf[] = new byte[1024];
        try {
            socket = new Socket(Server.HOST, Server.MASTER_PORT);
            out = socket.getOutputStream();
            buf[0] = MSG.HEAD_SLAVE;
            buf[1] = MSG.SLAVE_REGISTER;
            Util.getBytes(Server.CurPort, buf, 2, 2 + 4);
            out.write(buf, 0, 6);
            out.flush();
            in = socket.getInputStream();
            byte rsp[] = new byte[128];
            int len = in.read(buf, 0, buf.length);
            if(len >= 2 && buf[1] == MSG.MASTER_ACK_OK){
                System.out.println("slave register successfully, synchronizing data...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(in, out);
            Util.closeSocket(socket);
        }
    }


    /**
     * 解析命令行参数
     *
     * @param args : 命令行字符串数组
     */
    private void parseArgs(String args[]) {
        /**
         * command line
         * -m : 设置当前进程角色为master
         * -s : 设置当前进程角色为salve
         * -p : 设置当前进程监听端口（master不需要这个参数，默认在8080端口监听）
         * */
        Options opts = new Options();
        opts.addOption("h", false, "help info");
        opts.addOption("m", false, "set current process run as a master node");
        opts.addOption("s", false, "set current process run as a slave node");
        opts.addOption("p", true, "port for current server to listen on, port for master node is 8080 by default");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("options", opts);
                    System.exit(0);
                } else {
                    if (cl.hasOption("m")) {
                        Server.IsMaster = true;
                        Server.CurPort = Server.MASTER_PORT;
                    } else if (cl.hasOption("s")) {
                        Server.IsMaster = false;
                        if (!cl.hasOption("p")) {
                            throw new Exception();
                        }
                        // TODO
                        System.out.println(cl.getOptionValue("p"));
                        Server.CurPort = Integer.parseInt(cl.getOptionValue("p"));
                    }
                }
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.print("command line error:");
            for (String cmd : args) {
                System.out.print(cmd + " ");
            }
            System.out.println();
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("options", opts);
            System.exit(0);
        }
    }

}