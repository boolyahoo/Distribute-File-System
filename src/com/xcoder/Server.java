/**
 * Copyright : xcoder boolyahoo@gmail.com
 */

package com.xcoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.PrintStream;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.cli.*;

public class Server {
    private static PrintStream out = System.out;
    // port for master node
    public static int port = 8080;
    public static final String host = "localhost";
    public static boolean isMaster = true;


    public void run(String args[]) throws Exception {
        if (parseArgs(args)) {
            initServer();
        }

    }


    private void initServer() throws Exception {
        out.println("current node : " + (isMaster ? "master" : "slave"));
        out.println("port:" + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap sBoot = new ServerBootstrap();
        try {
            sBoot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(isMaster ? new ServerInitializer() : new SlaveInitializer());
            out.println("Server started");
            ChannelFuture cFuture = sBoot.bind(port).sync();
            cFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            out.println("Server closed");
        }
    }


    /**
     * parse args for current process
     *
     * @param args : 命令行
     * @return : true if args are all right
     */
    private boolean parseArgs(String args[]) {
        /**
         * command line
         * -m : set current server runs as a master node
         * -s : set current server runs as a slave node
         * -p : current server port
         * */
        Options opts = new Options();
        opts.addOption("h", false, "help info");
        opts.addOption("m", false, "set current process run as a master node");
        opts.addOption("s", false, "set current process run as a slave node");
        opts.addOption("p", false, "current server port");
        BasicParser parser = new BasicParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("options", opts);
                } else {
                    if (cl.hasOption("p")) {
                        port = Integer.parseInt(cl.getOptionValue("p"));
                    } else {
                        HelpFormatter hf = new HelpFormatter();
                        hf.printHelp("options", opts);
                        return false;
                    }
                    if (cl.hasOption("m")) {
                        isMaster = true;
                    } else if (cl.hasOption("s")) {
                        isMaster = false;
                    } else {
                        HelpFormatter hf = new HelpFormatter();
                        hf.printHelp("options", opts);
                        return false;
                    }
                }
            } else {
                out.println("command line error");
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("options", opts);
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }


    public static void main(String[] args) throws Exception {
        new Server().run(args);
    }
}