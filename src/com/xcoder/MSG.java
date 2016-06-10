package com.xcoder;

/**
 * Created by xcoder on 6/3/16.
 * 定义消息类型
 */

public class MSG {
    /** HeadType **/
    public static final byte HEAD_MASTER           = 0x00;
    /** OpType **/
    public static final byte MASTER_DEFAULT        = 0x00;
    public static final byte MASTER_ACK_OK         = 0x01;
    public static final byte MASTER_ACK_FAIL       = 0x02;


    public static final byte HEAD_SLAVE            = 0x01;
    public static final byte SLAVE_REGISTER        = 0x00;



    /** HeadType **/
    public static final byte HEAD_CLIENT           = 0x02;
    /** OpType **/
    public static final byte CLIENT_DEFAULT        = 0x00;
    public static final byte CLIENT_REGISTER       = 0x01;
    /** [HeadType(1B) OpType(1B)**/
    public static final byte CLIENT_QUERY_PWD      = 0x02;
    /** [HeadType(1B) OpType(1B) ClientID(8B)] **/
    public static final byte CLIENT_UPDATE_PWD     = 0x03;
    /** [HeadType(1B) OpType(1B) ClientID(8B) Dir(variable)] **/

    public static final byte CLIENT_CREATE_FILE    = 0x04;
    public static final byte CLIENT_DELETE_FILE    = 0x05;
    public static final byte CLIENT_UPDATE_FILE    = 0x06;
    public static final byte CLIENT_READ_FILE      = 0x07;





}
