package com.xcoder;

import java.io.BufferedReader;
import java.io.PrintWriter;

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
}
