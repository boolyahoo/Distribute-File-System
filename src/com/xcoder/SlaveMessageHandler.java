package com.xcoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.PrintStream;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler extends SimpleChannelInboundHandler<String> {

    private PrintStream out = System.out;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        out.println(s);
    }
}
