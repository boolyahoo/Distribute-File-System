package com.xcoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.IntegerHolder;

import java.io.PrintStream;

/**
 * Created by xcoder on 2016/4/23.
 * 客户端的请求在这里处理
 * 一个函数处理一种请求
 */

public class ServerMessageHandler extends SimpleChannelInboundHandler<String> {
    public static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup slaves = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private PrintStream out = System.out;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        allChannels.add(ctx.channel());
        out.println("allChannenls size :" + allChannels.size());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        allChannels.remove(ctx.channel());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        out.println("message thread ID " + Thread.currentThread().getId());
        Channel incoming = ctx.channel();
        out.println("message:" + message);
        for(Channel channel : slaves){
            channel.writeAndFlush(message + "\n");
        }
        byte type = message.getBytes()[0];
        switch (type) {
            case MSG.HEAD_CLIENT:
                for (Channel channel : allChannels) {
                    if (channel == incoming) {
                        allChannels.remove(channel);
                        clients.add(channel);
                        break;
                    }
                }
                incoming.writeAndFlush("/home/xcoder" + "\n");
                break;
            case MSG.HEAD_SLAVE:
                for (Channel channel : allChannels) {
                    if (channel == incoming) {
                        allChannels.remove(channel);
                        slaves.add(channel);
                        break;
                    }
                }
                incoming.writeAndFlush("slave registered" + "\n");
                break;
            default:
                break;
        }

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        out.println(incoming.remoteAddress() + ":online");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //
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
