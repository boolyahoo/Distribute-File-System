package com.xcoder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * Created by booly on 2016/5/26.
 */


public class SlaveMessageHandler implements Runnable {
    private Socket client = null;


    public SlaveMessageHandler(Socket client) {
        this.client = client;
    }


    public void run() {

    }
}
