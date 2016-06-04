/**
 * Copyright : xcoder boolyahoo@gmail.com
 */

package com.xcoder;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int port = 8080;
    public static final String host = "localhost";
    public static boolean isMaster = true;


    public void run(String args[]) throws Exception {
        parseArgs(args);
        if (isMaster) {
            initAsMaster();
        } else {
            initAsSlave();
        }
    }


    private void initAsMaster() throws Exception {
        System.out.println("current node : master");
        System.out.println("port : " + port);
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            Socket client = null;
            while (true) {
                // 有新的连接进入，新建一个线程处理
                client = server.accept();
                new Thread(new ServerMessageHandler(client, System.currentTimeMillis())).start();
            }
        } finally {
            if (server != null) {
                server.close();
                server = null;
            }
        }
    }


    private void initAsSlave() {
        System.out.println("current node : slave");
        Socket socket = null;
        BufferedReader socketIn = null;
        PrintWriter socketOut = null;
        try {
            // 向master发起连接
            socket = new Socket(host, port);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            // 向master发送消息表明自己是slave
            byte message[] = {MSG.HEAD_SLAVE};
            socketOut.println(new String(message));
            String resp = socketIn.readLine();
            System.out.println("message" + resp);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socketIn != null) {
                try {
                    socketIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socketIn = null;
            }
            if (socketOut != null) {
                socketOut.close();
                socketOut = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
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
         * */
        Options opts = new Options();
        opts.addOption("h", false, "help info");
        opts.addOption("m", false, "set current process run as a master node");
        opts.addOption("s", false, "set current process run as a slave node");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("options", opts);
                } else {
                    if (cl.hasOption("m")) {
                        isMaster = true;
                    } else if (cl.hasOption("s")) {
                        isMaster = false;
                    }
                }
            } else {
                System.out.println("command line error");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        new Server().run(args);
    }
}