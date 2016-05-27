package com.xcoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.PrintStream;

/**
 * Created by xcoder on 2016/4/23.
 * 客户端的请求在这里处理
 * 一个函数处理一种请求
 */

public class ServerMessageHandler extends SimpleChannelInboundHandler<String> {
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private PrintStream out = System.out;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " join\n");
        }
        //连接建立后将当前channel加入channels
        channels.add(ctx.channel());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " leave\n");
        }
        channels.remove(ctx.channel());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        Channel incoming = ctx.channel();
        out.println("client message:" + incoming.remoteAddress() + ":" + message);
        for (Channel channel : channels) {
            if (channel != incoming) {
                channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + message + "\n");
            } else {
                channel.writeAndFlush("[you]" + message + "\n");
            }
        }
        if(message.contains("close")){
            incoming.close();
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        out.println("Client:" + incoming.remoteAddress() + ":online");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        out.println("Client:" + incoming.remoteAddress() + ":offline");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        out.println("Client:" + incoming.remoteAddress() + ":exception");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
