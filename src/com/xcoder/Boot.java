/**
 * Copyright : xcoder boolyahoo@gmail.com
 */

package com.xcoder;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Boot用于启动当前Server，对Server进行基本配置
 * 包括server的角色（master or slave）
 * server监听的端口号（由命令行配置输入）
 */
public class Boot {


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


    private void registerSlave() {
        Socket socket = null;
        BufferedReader sin = null;
        PrintWriter sout = null;
        try {
            socket = new Socket(Server.HOST, Server.MASTER_PORT);
            sout = new PrintWriter(socket.getOutputStream(), true);
            byte head[] = {MSG.HEAD_SLAVE, MSG.SLAVE_REGISTER};
            sout.println(new String(head) + Server.CurPort);
            sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = sin.readLine();
            System.out.println("server:" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(sin, sout);
            Util.closeSocket(socket);
        }
    }


    /**
     * parse args for current process
     *
     * @param args : 命令行
     */
    private void parseArgs(String args[]) {
        /**
         * command line
         * -m : set current server runs as a master node
         * -s : set current server runs as a slave node
         * -p : port for current server to listen on
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
                    throw new Exception();
                } else {
                    if (cl.hasOption("m")) {
                        Server.IsMaster = true;
                        Server.CurPort = Server.MASTER_PORT;
                    } else if (cl.hasOption("s")) {
                        Server.IsMaster = false;
                        if (!cl.hasOption("p")) {
                            throw new Exception();
                        }
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


    public static void main(String[] args) throws Exception {
        new Boot().run(args);
    }
}