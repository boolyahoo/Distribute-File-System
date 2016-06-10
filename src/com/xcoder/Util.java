package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/6/4.
 */

public class Util {
    public static void closeStream(InputStream in, OutputStream out) {
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
     *
     * @param num   : long型数字
     * @param buf   ：分解得到的字节存放
     * @param start : 第一个字节在buf的下标
     * @param end   : 最后一个字节在buf中下一位下标
     */
    public static void getBytes(long num, byte buf[], int start, int end) {
        //获取num的字节数组，低字节表示高位
        int i = end;
        while (--i >= start) {
            buf[i] = (byte) (num & 0xFF);
            num = num >> 8;
        }
    }


    /**
     * 从buf中解析long
     * 低位字节表示高位数字
     *
     * @param buf   : 输入的字节数组，低位字节表示高位数字
     * @param start : 第一个有效字节的下标
     * @param end   ：最后一个有效字节下标的下一个下标
     * @return ：解析得到的long型数字
     */
    public static long parseNum(byte[] buf, int start, int end) {
        long value = 0;
        for (int i = start; i < end; i++) {
            value = (value << 8) + (buf[i] & 0xFF);
        }
        return value;
    }
}
