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

public class Server {
    private int port;
    private PrintStream out = System.out;


    public Server(int port) {
        this.port = port;
    }


    public void run() throws Exception {
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
            // 绑定端口，开始接收进来的连接
            ChannelFuture cFuture = sBoot.bind(port).sync();
            // 等待服务器 socket 关闭
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            cFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            out.println("Server closed");
        }
    }


    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new Server(port).run();
    }
}