/**
 * Copyright : xcoder boolyahoo@gmail.com
 */

package com.xcoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.PrintStream;

import org.apache.commons.cli.*;

public class Server {
    private static PrintStream out = System.out;
    private int port = 8080;
    private boolean isMaster = true;


    public Server() {

    }


    public void run(String args[]) throws Exception {

        String[] Args0 = {"-h"};
        String[] Args1 = {"-i", "192.168.1.1", "-p", "8443", "-t", "https"};
        Option help = new Option("h", "the command help");
        Option user = OptionBuilder.withArgName("type")
                .hasArg()
                .withDescription("target the search type").create("t");
        // 此处定义参数类似于 java 命令中的 -D<name>=<value>
        Option property = OptionBuilder.withArgName("property=value")
                .hasArgs(2)
                .withValueSeparator()
                .withDescription("search the objects which have the target property and value").create("D");
        Options opts = new Options();
        opts.addOption(help);
        opts.addOption(user);
        opts.addOption(property);





        //initServer();
    }


    private void initServer() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sBoot = new ServerBootstrap();
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


    public static void main(String[] args) throws Exception {
        /**
         * command line
         * -m : current server runs as a master node
         * -s : current server runs as a slave node
         * -p : current server port
         * */
        new Server().run(args);
    }
}