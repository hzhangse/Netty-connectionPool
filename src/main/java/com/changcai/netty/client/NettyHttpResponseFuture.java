/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.client;

import io.netty.channel.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.changcai.netty.util.NettyHttpResponseBuilder;
/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpResponseFuture {
    private final CountDownLatch              latch       = new CountDownLatch(1);

    private volatile boolean                  isDone      = false;

    private volatile boolean                  isCancel    = false;

    private final AtomicBoolean               isProcessed = new AtomicBoolean(false);

    private volatile NettyHttpResponseBuilder responseBuilder;

    private volatile Channel                  channel;

    public boolean cancel(Throwable cause) {
        if (isProcessed.getAndSet(true)) {
            return false;
        }

        responseBuilder = new NettyHttpResponseBuilder();
        responseBuilder.setSuccess(false);
        responseBuilder.setCause(cause);
        isCancel = true;
        latch.countDown();
        return true;
    }

    public NettyHttpResponse get() throws InterruptedException, ExecutionException {
        latch.await();
        return responseBuilder.build();
    }

    public NettyHttpResponse get(long timeout, TimeUnit unit) throws TimeoutException,
                                                             InterruptedException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException();
        }
        return responseBuilder.build();
    }

    public boolean done() {
        if (isProcessed.getAndSet(true)) {
            return false;
        }
        isDone = true;
        latch.countDown();
        return true;
    }

    public boolean isCancelled() {
        return isCancel;
    }

    public boolean isDone() {
        return isDone;
    }

    /**
     * Getter method for property <tt>channel</tt>.
     * 
     * @return property value of channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Setter method for property <tt>channel</tt>.
     * 
     * @param channel value to be assigned to property channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Getter method for property <tt>responseBuilder</tt>.
     * 
     * @return property value of responseBuilder
     */
    public NettyHttpResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

    /**
     * Setter method for property <tt>responseBuilder</tt>.
     * 
     * @param responseBuilder value to be assigned to property responseBuilder
     */
    public void setResponseBuilder(NettyHttpResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }
}
