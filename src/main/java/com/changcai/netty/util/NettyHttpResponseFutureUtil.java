/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.util;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

import com.changcai.netty.client.NettyHttpResponseFuture;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpResponseFutureUtil {

    private static final AttributeKey<Object> DEFAULT_ATTRIBUTE       = AttributeKey
                                                                          .valueOf("nettyHttpResponse");

    private static final AttributeKey<Object> ROUTE_ATTRIBUTE         = AttributeKey
                                                                          .valueOf("route");

    private static final AttributeKey<Object> FORCE_CONNECT_ATTRIBUTE = AttributeKey
                                                                          .valueOf("forceConnect");

    public static void attributeForceConnect(Channel channel, boolean forceConnect) {
        if (forceConnect) {
            channel.attr(FORCE_CONNECT_ATTRIBUTE).set(true);
        }
    }

    public static void attributeResponse(Channel channel, NettyHttpResponseFuture responseFuture) {
        channel.attr(DEFAULT_ATTRIBUTE).set(responseFuture);
        responseFuture.setChannel(channel);
    }

    public static void attributeRoute(Channel channel, InetSocketAddress route) {
        channel.attr(ROUTE_ATTRIBUTE).set(route);
    }

    public static NettyHttpResponseFuture getResponse(Channel channel) {
        return (NettyHttpResponseFuture) channel.attr(DEFAULT_ATTRIBUTE).get();
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

    public static void setPendingResponse(Channel channel, HttpResponse pendingResponse) {
        NettyHttpResponseFuture responseFuture = getResponse(channel);
        NettyHttpResponseBuilder responseBuilder = new NettyHttpResponseBuilder();
        responseBuilder.setSuccess(true);
        responseBuilder.setPendingResponse(pendingResponse);
        responseFuture.setResponseBuilder(responseBuilder);
    }

    public static boolean headerContainConnectionClose(Channel channel) {
        NettyHttpResponseFuture responseFuture = getResponse(channel);
        return HttpHeaders.Values.CLOSE.toString().equalsIgnoreCase(responseFuture.getResponseBuilder()
            .getPendingResponse().headers().get(HttpHeaders.Names.CONNECTION));
    }

    public static void setPendingContent(Channel channel, HttpContent httpContent) {
        NettyHttpResponseFuture responseFuture = getResponse(channel);
        NettyHttpResponseBuilder responseBuilder = responseFuture.getResponseBuilder();
        responseBuilder.addContent(httpContent.content().retain());
    }

    public static boolean done(Channel channel) {
        NettyHttpResponseFuture responseFuture = getResponse(channel);
        if (null != responseFuture) {
            return responseFuture.done();
        }

        return true;
    }

    public static boolean cancel(Channel channel, Throwable cause) {
        NettyHttpResponseFuture responseFuture = getResponse(channel);
        return responseFuture.cancel(cause);
    }
}
