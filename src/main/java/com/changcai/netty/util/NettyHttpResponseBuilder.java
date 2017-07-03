/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.changcai.netty.client.NettyHttpResponse;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpResponseBuilder {

    private volatile HttpResponse      pendingResponse;

    private volatile List<ByteBuf>     pendingContents;

    private volatile Throwable         cause;

    private volatile NettyHttpResponse content;

    private AtomicBoolean              isBuild = new AtomicBoolean(false);

    private volatile boolean           success = false;

    public NettyHttpResponse build() {
        if (isBuild.getAndSet(true)) {
            return content;
        }
        NettyHttpResponse response = new NettyHttpResponse();
        content = response;

        if (success) {
            response.setSuccess(true);
            response.setVersion(pendingResponse.getProtocolVersion());
            response.setStatus(pendingResponse.getStatus());
            response.setHeaders(pendingResponse.headers());
            response.setContents(pendingContents);
        } else {
            response.setCause(cause);
        }
        return content;
    }

    public void addContent(ByteBuf byteBuf) {
        if (null == pendingContents) {
            pendingContents = new ArrayList<ByteBuf>();
        }
        pendingContents.add(byteBuf);
    }

    /**
     * @return the contents
     */
    public List<ByteBuf> getContents() {
        return pendingContents;
    }

    /**
     * Getter method for property <tt>pendingResponse</tt>.
     * 
     * @return property value of pendingResponse
     */
    public HttpResponse getPendingResponse() {
        return pendingResponse;
    }

    /**
     * Setter method for property <tt>pendingResponse</tt>.
     * 
     * @param pendingResponse value to be assigned to property pendingResponse
     */
    public void setPendingResponse(HttpResponse pendingResponse) {
        this.pendingResponse = pendingResponse;
    }

    /**
     * Getter method for property <tt>pendingContents</tt>.
     * 
     * @return property value of pendingContents
     */
    public List<ByteBuf> getPendingContents() {
        return pendingContents;
    }

    /**
     * Setter method for property <tt>pendingContents</tt>.
     * 
     * @param pendingContents value to be assigned to property pendingContents
     */
    public void setPendingContents(List<ByteBuf> pendingContents) {
        this.pendingContents = pendingContents;
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
}
