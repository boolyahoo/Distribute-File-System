package com.xcoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by booly on 2016/6/4.
 */

public class Util {
    public static void closeStream(BufferedReader in, PrintWriter out) {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 将数字long分解为字节数组表示，低位字节表示高位数字
     * @param num   : long型数字
     * @param buf   ：分解得到的字节存放
     * @param index : buf中用于存放第一个字节的下标
     */
    public static void getBytes(long num, byte buf[], int index) {
        //获取num的字节数组，低字节表示高位
        int i = buf.length;
        while (--i >= 0) {
            buf[i + index] = (byte) (num & 0xFF);
            num = num >> 8;
        }
    }


    /**
     * 从buf中解析long
     * 低位字节表示高位数字
     *
     * @param buf    : 输入的字节数组，低位字节表示高位数字
     * @param begain : 第一个有效字节的下标
     * @param end    ：最后一个有效字节下标的下一个下标
     * @return ：解析得到的long型数字
     */
    public static long parseNum(byte[] buf, int begain, int end) {
        long value = 0;
        for (int i = begain; i < end; i++) {
            value = (value << 8) + (buf[i] & 0xFF);
        }
        return value;
    }
}
