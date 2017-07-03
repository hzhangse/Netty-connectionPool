/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.changcai.netty.client.NettyTCPResponseFuture;
import com.changcai.netty.pool.ChannelStatus;
import com.changcai.netty.pool.NettyTCPChannelPool;
import com.changcai.netty.util.NettyTCPResponseBuilder;
import com.changcai.netty.util.NettyTCPResponseFutureUtil;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyTCPChannelPoolHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(NettyTCPChannelPoolHandler.class
                                           .getName());

    private NettyTCPChannelPool channelPool;

    /**
     * @param channelPool
     */
    public NettyTCPChannelPoolHandler(NettyTCPChannelPool channelPool) {
        super();
        this.channelPool = channelPool;
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext,
     *      java.lang.Object)
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
    public void setChannelPool(NettyTCPChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        NettyTCPResponseFutureUtil.setStauts(ctx.channel(),ChannelStatus.ACTIVE.getValue());
    }

  

//  @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//       super.channelReadComplete(ctx);
//        NettyTCPResponseFuture future = NettyTCPResponseFutureUtil.getResponse(ctx.channel());
//        NettyTCPResponseBuilder builder = future.getResponseBuilder();
//        if (builder != null&&!future.isDone()) {
//            NettyTCPResponseFutureUtil.done(ctx.channel());
//            channelPool.returnChannel(ctx.channel());
//        }
//    }

 

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NettyTCPResponseFutureUtil.setStauts(ctx.channel(),ChannelStatus.INACTIVE.getValue());
    }

    //@Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyTCPResponseFutureUtil.setPendingResponse(ctx.channel());
        ByteBuf buf = (ByteBuf) msg;
        NettyTCPResponseFutureUtil.setPendingContent(ctx.channel(), buf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        messageReceived(ctx, msg);
    }
}
