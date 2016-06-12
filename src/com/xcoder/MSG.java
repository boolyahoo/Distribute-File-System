package com.xcoder;

/**
 * Created by xcoder on 6/3/16.
 * 定义消息类型
 */

public class MSG {
    public static final byte HEAD_MASTER           = 0x00;
    public static final byte MASTER_DEFAULT        = 0x00;
    public static final byte MASTER_ACK_OK         = 0x01;
    public static final byte MASTER_ACK_FAIL       = 0x02;
    public static final byte MASTER_CREATE_FILE    = 0x03;
    public static final byte MASTER_DELETE_FILE    = 0x04;


    public static final byte HEAD_SLAVE            = 0x01;
    public static final byte SLAVE_REGISTER        = 0x00;
    public static final byte SLAVE_ACK_OK          = 0x01;
    public static final byte SLAVE_ACK_FAIL        = 0x02;

    public static final byte HEAD_CLIENT           = 0x02;
    public static final byte CLIENT_DEFAULT        = 0x00;
    public static final byte CLIENT_REGISTER       = 0x01;
    public static final byte CLIENT_QUERY_PWD      = 0x02;
    public static final byte CLIENT_UPDATE_PWD     = 0x03;
    public static final byte CLIENT_CREATE_FILE    = 0x04;
    public static final byte CLIENT_DELETE_FILE    = 0x05;
    public static final byte CLIENT_UPDATE_FILE    = 0x06;
    public static final byte CLIENT_READ_FILE      = 0x07;

    public static final byte FILE_DIR              = 0x00;
    public static final byte FILE_COMN             = 0x01;

    public static final byte SYNC_OK               = 0x00;
    public static final byte SYNC_OCCUPIED         = 0x01;
    public static final byte SYNC_NOT_EXIST        = 0x02;



}
