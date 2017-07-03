/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.pool;

/**
 * 
 * @author ryan
 * @version $Id: ChannelStatus.java, v 0.1 2016年9月27日 下午7:20:42 ryan Exp $
 */
public enum ChannelStatus {
    
    INIT("init"), ACTIVE("active"),INACTIVE("inactive"),
    DISCONNECTED("disconnected"), 
    CANCEL("cancel"), 
    DONE("done"), 
    READCOMPLETE("readcomplete");
    
    private String value;

    ChannelStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;

    }

    public void setValue(String value) {
        this.value = value;
    }
}
