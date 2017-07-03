/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.changcai.netty.client.NettyTCPResponseFuture;
import com.changcai.netty.pool.ChannelStatus;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyTCPResponseFutureUtil {
    private static final Logger               logger                  = LoggerFactory
                                                                          .getLogger(NettyTCPResponseFutureUtil.class
                                                                              .getName());
    private static final AttributeKey<Object> DEFAULT_ATTRIBUTE       = AttributeKey
                                                                          .valueOf("nettyTcpResponse");

    private static final AttributeKey<Object> ROUTE_ATTRIBUTE         = AttributeKey
                                                                          .valueOf("route");

    private static final AttributeKey<Object> FORCE_CONNECT_ATTRIBUTE = AttributeKey
                                                                          .valueOf("forceConnect");

    private static final AttributeKey<Object> STATUS_ATTRIBUTE        = AttributeKey
                                                                          .valueOf("status");

    public static void attributeForceConnect(Channel channel, boolean forceConnect) {
        if (forceConnect) {
            channel.attr(FORCE_CONNECT_ATTRIBUTE).set(true);
        }
    }

    public static void setStauts(Channel channel, String status) {
        channel.attr(STATUS_ATTRIBUTE).set(status);
    }

    public static String getStauts(Channel channel) {
        return (String) channel.attr(STATUS_ATTRIBUTE).get();
    }

    public static void attributeResponse(Channel channel, NettyTCPResponseFuture responseFuture) {
        channel.attr(DEFAULT_ATTRIBUTE).set(responseFuture);
        responseFuture.setChannel(channel);
    }

    public static void attributeRoute(Channel channel, InetSocketAddress route) {
        channel.attr(ROUTE_ATTRIBUTE).set(route);
    }

    public static NettyTCPResponseFuture getResponse(Channel channel) {
        return (NettyTCPResponseFuture) channel.attr(DEFAULT_ATTRIBUTE).get();
    }

    public static InetSocketAddress getRoute(Channel channel) {
        return (InetSocketAddress) channel.attr(ROUTE_ATTRIBUTE).get();
    }

    public static boolean getForceConnect(Channel channel) {
        Object forceConnect = channel.attr(FORCE_CONNECT_ATTRIBUTE).get();
        if (null == forceConnect) {
            return false;
        }
        return true;
    }

    public static void setPendingResponse(Channel channel) {
        NettyTCPResponseFuture responseFuture = getResponse(channel);
        if (responseFuture.getResponseBuilder() == null) {
            NettyTCPResponseBuilder responseBuilder = new NettyTCPResponseBuilder();
            responseBuilder.setSuccess(true);
            responseFuture.setResponseBuilder(responseBuilder);
        }
    }

    public static void setPendingContent(Channel channel, ByteBuf tcpContent) {
        NettyTCPResponseFuture responseFuture = getResponse(channel);
        NettyTCPResponseBuilder responseBuilder = responseFuture.getResponseBuilder();
        responseBuilder.addContent(tcpContent.retain());
    }

    public static boolean done(Channel channel) {
        NettyTCPResponseFuture responseFuture = getResponse(channel);
        if (null != responseFuture) {
            return responseFuture.done();
        }

        return true;
    }

    public static boolean cancel(Channel channel, Throwable cause) {
        NettyTCPResponseFuture responseFuture = getResponse(channel);
        return responseFuture.cancel(cause);
    }
}
