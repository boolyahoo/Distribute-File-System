package com.xcoder;

/**
 * Created by xcoder on 6/3/16.
 */

public class MSG {
    public static final byte HEAD_MASTER = 0x00;
    public static final byte HEAD_SLAVE = 0x01;
    public static final byte HEAD_CLIENT = 0x02;

    public static final byte CLIENT_OP_DEFULT = 0x00;
    public static final byte CLIENT_QUERY_PWD = 0x01;
    public static final byte CLIENT_UPDATE_PWD = 0x02;
    public static final byte CLIENT_CREATE_FILE = 0x03;
    public static final byte CLIENT_DELETE_FILE = 0x04;
    public static final byte CLIENT_UPDATE_FILE = 0x05;
    public static final byte CLIENT_READ_FILE = 0x06;

    public static final byte SLAVE_OP_DEFULT = 0x00;



}
