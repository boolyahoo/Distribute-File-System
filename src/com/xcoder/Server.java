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
    public static final int port = 8080;
    public static final String host = "localhost";
    public static boolean isMaster = true;


    public void run(String args[]) throws Exception {
        parseArgs(args);
        if (isMaster) {
            initAsMaster();
        }else{
            initAsSlave();
        }
    }


    private void initAsMaster() throws Exception {
        out.println("current node : master");
        out.println("port:" + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap sBoot = new ServerBootstrap();
        try {
            sBoot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            out.println("Server started");
            ChannelFuture cFuture = sBoot.bind(port).sync();
            cFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            out.println("Server closed");
        }
    }


    private void initAsSlave(){
        out.println("current node : slave");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bStrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SlaveInitializer());
            bStrap.connect(host, port).sync().channel();
            out.println("slave started!");
            while(true){
                // 添加空转，防止slave退出
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
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
         * -p : current server port
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
                out.println("command line error");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        new Server().run(args);
    }
}