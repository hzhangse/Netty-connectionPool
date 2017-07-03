/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.client;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpResponse {
    private volatile boolean            success = false;
    private volatile HttpResponseStatus status;
    private volatile HttpVersion        version;
    private volatile HttpHeaders        headers;
    private volatile List<ByteBuf>      contents;
    private volatile Throwable          cause;

    public NettyHttpResponse() {
        super();
    }

    public String getResponseBody() {
        return getResponseBody(Charset.forName("GBK"));
    }

    public String getResponseBody(Charset charset) {
        if (null == contents || 0 == contents.size()) {
            return null;
        }
        StringBuilder responseBody = new StringBuilder();
        for (ByteBuf content : contents) {
            responseBody.append(content.toString(charset));
        }

        return responseBody.toString();
    }

    public void addContent(ByteBuf byteBuf) {
        if (null == contents) {
            contents = new ArrayList<ByteBuf>();
        }
        contents.add(byteBuf);
    }

    /**
     * @return the version
     */
    public HttpVersion getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(HttpVersion version) {
        this.version = version;
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
     * @return the headers
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * @return the status
     */
    public HttpResponseStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(HttpResponseStatus status) {
        this.status = status;
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
