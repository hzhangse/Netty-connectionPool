/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.client;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyTCPResponse {
    private volatile boolean       success = false;
    private volatile List<ByteBuf> contents;
    private volatile Throwable     cause;

    public NettyTCPResponse() {
        super();
    }

    public String getResponseBody() {
        return getResponseBody(Charset.forName("GBK"));
    }

    public String getResponseBody(Charset charset) {
        if (this.isSuccess()) {
            if (null == contents || 0 == contents.size()) {
                return null;
            }
            StringBuffer responseBody = new StringBuffer();
            Iterator<ByteBuf> iter = contents.iterator();
            while (iter.hasNext()){
                ByteBuf appendContent = iter.next();
                responseBody.append(appendContent.toString(charset));
            }
            
            return responseBody.toString();
        }else {
            return cause.getMessage();
        }
    }

    public void addContent(ByteBuf byteBuf) {
        if (null == contents) {
            contents = new ArrayList<ByteBuf>();
        }
        contents.add(byteBuf);
    }

    /**
     * @return the contents
     */
    public List<ByteBuf> getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(List<ByteBuf> contents) {
        this.contents = contents;
    }

    /**
     * Getter method for property <tt>success</tt>.
     * 
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     * 
     * @param success value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter method for property <tt>cause</tt>.
     * 
     * @return property value of cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Setter method for property <tt>cause</tt>.
     * 
     * @param cause value to be assigned to property cause
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

}
