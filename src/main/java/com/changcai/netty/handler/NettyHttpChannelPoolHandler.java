/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.changcai.netty.pool.NettyHttpChannelPool;
import com.changcai.netty.util.NettyHttpResponseFutureUtil;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpChannelPoolHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpChannelPoolHandler.class.getName());

    private NettyHttpChannelPool    channelPool;

    /**
     * @param channelPool
     */
    public NettyHttpChannelPoolHandler(NettyHttpChannelPool channelPool) {
        super();
        this.channelPool = channelPool;
    }



    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            logger.warn("remove idle channel: " + ctx.channel());
            ctx.channel().close();
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * @param channelPool
     *            the channelPool to set
     */
    public void setChannelPool(NettyHttpChannelPool channelPool) {
        this.channelPool = channelPool;
    }

	
	protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		  if (msg instanceof HttpResponse) {
	            HttpResponse headers = (HttpResponse) msg;
	            NettyHttpResponseFutureUtil.setPendingResponse(ctx.channel(), headers);
	        }
	        
	        if (msg instanceof HttpContent) {
	            HttpContent httpContent = (HttpContent) msg;
	            NettyHttpResponseFutureUtil.setPendingContent(ctx.channel(), httpContent);
	            if (httpContent instanceof LastHttpContent) {
	                boolean connectionClose = NettyHttpResponseFutureUtil
	                    .headerContainConnectionClose(ctx.channel());

	                NettyHttpResponseFutureUtil.done(ctx.channel());
	                //the maxKeepAliveRequests config will cause server close the channel, and return 'Connection: close' in headers                
	                if (!connectionClose) {
	                    channelPool.returnChannel(ctx.channel());
	                }
	            }
	        }
		
	}



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        messageReceived(ctx,msg);
    }
}
