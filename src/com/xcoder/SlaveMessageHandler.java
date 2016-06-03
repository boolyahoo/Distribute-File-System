package com.xcoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.PrintStream;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler extends SimpleChannelInboundHandler<String> {
    private PrintStream out = System.out;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        out.println("message thread ID : " + Thread.currentThread().getId());
        out.println(message);
        Channel channel = ctx.channel();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        out.println("channel active!");
        out.println("channel remote address : " + channel.remoteAddress());
        out.println("channel local address : " + channel.localAddress());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        incoming.close();
    }
}
