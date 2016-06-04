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
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            out = new PrintWriter(this.client.getOutputStream(), true);
            String currentTime = null;
            String body = null;
            while (true) {
                body = in.readLine();
                if (body == null)
                    break;
                System.out.println("server read order : " + body);
                currentTime = "query time order".equalsIgnoreCase(body) ?
                        (new Date(System.currentTimeMillis()).toString()) : ("bad order");
                out.println(currentTime);

            }
        } catch (Exception e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                client = null;
            }
        }
    }
}
