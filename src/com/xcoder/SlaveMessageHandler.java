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
    private Socket client;
    private long socketID;
    private int type;


    public SlaveMessageHandler(Socket client, long socketID, int type) {
        this.client = client;
        this.socketID = socketID;
        this.type = type;
    }


    public void run() {
        while(true){

        }
    }
}
