/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.changcai.netty.client.NettyTCPRequest;
import com.changcai.netty.client.NettyTCPResponseFuture;
import com.changcai.netty.handler.AdditionalChannelInitializer;
import com.changcai.netty.handler.NettyTCPChannelPoolHandler;
import com.changcai.netty.util.NettyTCPResponseFutureUtil;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyTCPChannelPool {
    private static final Logger                                 logger                = LoggerFactory
                                                                                          .getLogger(NettyTCPChannelPool.class
                                                                                              .getName());

    // channel pools per route
    private ConcurrentMap<String, LinkedBlockingQueue<Channel>> routeToPoolChannels;

    // max number of channels allow to be created per route
    private ConcurrentMap<String, Semaphore>                    maxPerRoute;

    // max time wait for a channel return from pool
    private int                                                 connectTimeOutInMilliSecondes;

    // max idle time for a channel before close
    private int                                                 maxIdleTimeInMilliSecondes;

    private AdditionalChannelInitializer                        additionalChannelInitializer;

    /**
     * value is true indicates that when there is not any channel in pool and
     * no new channel allowed to be create based on maxPerRoute, a new channel
     * will be forced to create.Otherwise, a <code>TimeoutException</code> will
     * be thrown
     * */
    private boolean                                             forceConnect;

    // default max number of channels allow to be created per route
    private final static int                                    DEFAULT_MAX_PER_ROUTE = 200;

    private EventLoopGroup                                      group;

    private final Bootstrap                                     clientBootstrap;

    private static final String                                 COLON                 = ":";

    /**
     * Create a new instance of ChannelPool
     * 
     * @param maxPerRoute
     *            max number of channels per route allowed in pool
     * @param connectTimeOutInMilliSecondes
     *            max time wait for a channel return from pool
     * @param maxIdleTimeInMilliSecondes
     *            max idle time for a channel before close
     * @param forceConnect
     *            value is false indicates that when there is not any channel in
     *            pool and no new channel allowed to be create based on
     *            maxPerRoute, a new channel will be forced to create.Otherwise,
     *            a <code>TimeoutException</code> will be thrown. The default
     *            value is false.
     * @param additionalChannelInitializer
     *            user-defined initializer
     * @param options
     *            user-defined options
     * @param customGroup
     *            user defined {@link EventLoopGroup}
     */
    @SuppressWarnings("unchecked")
    public NettyTCPChannelPool(Map<String, Integer> maxPerRoute, int connectTimeOutInMilliSecondes,
                               int maxIdleTimeInMilliSecondes, boolean forceConnect,
                               AdditionalChannelInitializer additionalChannelInitializer,
                               Map<ChannelOption, Object> options, EventLoopGroup customGroup) {

        this.additionalChannelInitializer = additionalChannelInitializer;
        this.maxIdleTimeInMilliSecondes = maxIdleTimeInMilliSecondes;
        this.connectTimeOutInMilliSecondes = connectTimeOutInMilliSecondes;
        this.maxPerRoute = new ConcurrentHashMap<String, Semaphore>();
        this.routeToPoolChannels = new ConcurrentHashMap<String, LinkedBlockingQueue<Channel>>();
        this.group = null == customGroup ? new NioEventLoopGroup() : customGroup;
        this.forceConnect = forceConnect;

        this.clientBootstrap = new Bootstrap();
        clientBootstrap.group(group).channel(NioSocketChannel.class)
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 4096, 65536))
            .option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    //   ch.pipeline().addLast("log", new LoggingHandler(LogLevel.WARN));

                    if (null != NettyTCPChannelPool.this.additionalChannelInitializer) {
                        NettyTCPChannelPool.this.additionalChannelInitializer.initChannel(ch);
                    }

                    ch.pipeline().addLast(
                        IdleStateHandler.class.getSimpleName(),
                        new IdleStateHandler(0, 0,
                            NettyTCPChannelPool.this.maxIdleTimeInMilliSecondes,
                            TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(LengthFieldBasedFrameDecoder.class.getSimpleName(),new LengthFieldBasedFrameDecoder());
                    ch.pipeline().addLast(NettyTCPChannelPoolHandler.class.getSimpleName(),
                        new NettyTCPChannelPoolHandler(NettyTCPChannelPool.this));
                }

            });
        if (null != options) {
            for (Entry<ChannelOption, Object> entry : options.entrySet()) {
                clientBootstrap.option(entry.getKey(), entry.getValue());
            }
        }

        if (null != maxPerRoute) {
            for (Entry<String, Integer> entry : maxPerRoute.entrySet()) {
                this.maxPerRoute.put(entry.getKey(), new Semaphore(entry.getValue()));
            }
        }
        
    }

    /**
     * send http request to server specified by the route. The channel used to
     * send the request is obtained according to the follow rules
     * <p>
     * 1. poll the first valid channel from pool without waiting. If no valid
     * channel exists, then go to step 2. 2. create a new channel and return. If
     * failed to create a new channel, then go to step 3. Note: the new channel
     * created in this step will be returned to the pool 3. poll the first valid
     * channel from pool within specified waiting time. If no valid channel
     * exists and the value of forbidForceConnect is false, then throw
     * <code>TimeoutException</code>. Otherwise,go to step 4. 4. create a new
     * channel and return. Note: the new channel created in this step will not
     * be returned to the pool.
     * </p>
     * 
     * @param route
     *            target server
     * @param request
     *            {@link HttpRequest}
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws IOException
     * @throws Exception
     */
    public NettyTCPResponseFuture sendRequest(InetSocketAddress route, final NettyTCPRequest request)
                                                                                                     throws InterruptedException,
                                                                                                     IOException {
        final NettyTCPResponseFuture responseFuture = new NettyTCPResponseFuture();
        //策略1：首先从池中获取连接（调用LinkedBlockingQueue.pool()，不等待，获取不到立即返回null）,
        //如果获取不到连接，则进入第二种策略
        if (sendRequestUsePooledChannel(route, request, responseFuture, false)) {
            logger.info("get channel from pool no wait");
            return responseFuture;
        }

        //策略2：创建新连接，如果信号量已用完或者创建连接失败，则进入第三种策略
        if (sendRequestUseNewChannel(route, request, responseFuture, forceConnect)) {
            logger.info("get channel created by self ");
            return responseFuture;
        }

        if (sendRequestUsePooledChannel(route, request, responseFuture, true)) {
            logger.info("get channel from pool by wait");
            return responseFuture;
        }
        throw new IOException("send request failed");
    }

    /**
     * return the specified channel to pool
     * 
     * @param channel
     */
    public void returnChannel(Channel channel) {
        if (NettyTCPResponseFutureUtil.getForceConnect(channel)) {
            return;
        }
        InetSocketAddress route = (InetSocketAddress) channel.remoteAddress();
        String key = getKey(route);
        LinkedBlockingQueue<Channel> poolChannels = routeToPoolChannels.get(key);

        if (null != channel && channel.isActive()) {
            if (poolChannels.offer(channel)) {
                logger.info(channel + " returned");
            }
        }
    }

    /**
     * close all channels in the pool and shut down the eventLoopGroup
     * 
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        for (LinkedBlockingQueue<Channel> queue : routeToPoolChannels.values()) {
            for (Channel channel : queue) {
                removeChannel(channel, null);
                channelGroup.add(channel);
            }
        }
        channelGroup.close().sync();
        group.shutdownGracefully();
    }

    /**
     * remove the specified channel from the pool,cancel the responseFuture and
     * release semaphore for the route
     * 
     * @param channel
     */
    private void removeChannel(Channel channel, Throwable cause) {

        InetSocketAddress route = (InetSocketAddress) channel.remoteAddress();
        String key = getKey(route);

        NettyTCPResponseFutureUtil.cancel(channel, cause);

        if (!NettyTCPResponseFutureUtil.getForceConnect(channel)) {
            LinkedBlockingQueue<Channel> poolChannels = routeToPoolChannels.get(key);
            if (poolChannels.remove(channel)) {
                logger.info(channel + " removed");
            }
            getAllowCreatePerRoute(key).release();
        }
    }

    /**
     * 从池中获取连接（调用LinkedBlockingQueue.pool()，不等待，获取不到立即返回null）,
     * 如果获取不到连接，则进入第二种策略
     * @param route
     * @param request
     * @param responseFuture
     * @param isWaiting
     * @return
     * @throws InterruptedException
     */
    private boolean sendRequestUsePooledChannel(InetSocketAddress route,
                                                final NettyTCPRequest request,
                                                NettyTCPResponseFuture responseFuture,
                                                boolean isWaiting) throws InterruptedException {

        LinkedBlockingQueue<Channel> poolChannels = getPoolChannels(getKey(route));
        Channel channel = poolChannels.poll();

        while (null != channel && !channel.isActive()) {
            channel = poolChannels.poll();
        }

        if (null == channel || !channel.isActive()) {
            if (!isWaiting) {
                return false;
            }
            channel = poolChannels.poll(connectTimeOutInMilliSecondes, TimeUnit.MILLISECONDS);
            if (null == channel || !channel.isActive()) {
                //means channel is closed,need re check the condition if can recreate channel
                if (null == channel) {
                    logger.warn("obtain channel from pool timeout");

                } else if (!channel.isActive()) {
                    logger.warn(" obtain channel from pool is not active!");

                }
                return false;
            }
        }

        logger.info(channel + " reuse");

        NettyTCPResponseFutureUtil.attributeResponse(channel, responseFuture);

        channel.writeAndFlush(request.getContent()).addListener(
            ChannelFutureListener.CLOSE_ON_FAILURE);
        return true;
    }

    private boolean sendRequestUseNewChannel(final InetSocketAddress route,
                                             final NettyTCPRequest request,
                                             final NettyTCPResponseFuture responseFuture,
                                             boolean forceConnect) {
        ChannelFuture future = createChannelFuture(route, forceConnect);
        if (null != future) {
            NettyTCPResponseFutureUtil.attributeResponse(future.channel(), responseFuture);
            NettyTCPResponseFutureUtil.setStauts(future.channel(), ChannelStatus.INIT.getValue());
            NettyTCPResponseFutureUtil.attributeRoute(future.channel(), route);
            future.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    // 新建连接成功，此时为新建channel的closeFuture添加listener，执行信号量恢复操作。
                    if (future.isSuccess()) {
                        future.channel().closeFuture().addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                NettyTCPResponseFutureUtil.done(future.channel());
                                returnChannel(future.channel());
//                                Throwable cause = future.cause();
//                                if (future.cause() == null) {
//                                    cause = new Throwable(ChannelStatus.DISCONNECTED.getValue());
//                                }
//                                removeChannel(future.channel(), cause);
//                                logger.error(future + " channel:" + future.channel()
//                                             + " closed, exception: " + future.cause());
                            }

                        });
                        future.channel().writeAndFlush(request.getContent())
                            .addListener(CLOSE_ON_FAILURE);
                    }
                    // 如果future的isSuccess返回false，则说明新建连接失败，需要恢复信号量；
                    else {
                        logger.warn(future.channel() + " connect failed, exception: "
                                    + future.cause());

                        NettyTCPResponseFutureUtil.cancel(future.channel(), future.cause());
                        //不属于forcecreate 类型的channel 需要释放信号量
                        if (!NettyTCPResponseFutureUtil.getForceConnect(future.channel())) {
                            releaseCreatePerRoute(future.channel());
                        }
                    }
                }

            });
            return true;
        }
        return false;
    }

    //释放信号量
    public void releaseCreatePerRoute(Channel channel) {
        InetSocketAddress route = NettyTCPResponseFutureUtil.getRoute(channel);
        getAllowCreatePerRoute(getKey(route)).release();
    }

    /**
     * 
     * @param key
     * @return
     */
    private Semaphore getAllowCreatePerRoute(String key) {
        Semaphore allowCreate = maxPerRoute.get(key);
        if (null == allowCreate) {
            Semaphore newAllowCreate = new Semaphore(DEFAULT_MAX_PER_ROUTE);
            allowCreate = maxPerRoute.putIfAbsent(key, newAllowCreate);
            if (null == allowCreate) {
                allowCreate = newAllowCreate;
            }
        }
        return allowCreate;
    }

    private LinkedBlockingQueue<Channel> getPoolChannels(String route) {
        LinkedBlockingQueue<Channel> oldPoolChannels = routeToPoolChannels.get(route);
        if (null == oldPoolChannels) {
            LinkedBlockingQueue<Channel> newPoolChannels = new LinkedBlockingQueue<Channel>();
            oldPoolChannels = routeToPoolChannels.putIfAbsent(route, newPoolChannels);
            if (null == oldPoolChannels) {
                oldPoolChannels = newPoolChannels;
            }
        }
        return oldPoolChannels;
    }

    private String getKey(InetSocketAddress route) {
        return route.getHostName() + COLON + route.getPort();
    }

    /**
     * 创建新连接
     * @param route
     * @param forceConnect
     * @return
     */
    private ChannelFuture createChannelFuture(InetSocketAddress route, boolean forceConnect) {
        String key = getKey(route);
        Semaphore allowCreate = getAllowCreatePerRoute(key);
        if (allowCreate.tryAcquire()) {
            try {
                ChannelFuture connectFuture = clientBootstrap.connect(route.getHostName(),
                    route.getPort());
                return connectFuture;
            } catch (Exception e) {
                logger.error("connect failed", e);
                allowCreate.release();
            }
        }
        if (forceConnect) {
            ChannelFuture connectFuture = clientBootstrap.connect(route.getHostName(),
                route.getPort());
            if (null != connectFuture) {
                NettyTCPResponseFutureUtil.attributeForceConnect(connectFuture.channel(),
                    forceConnect);
            }
            return connectFuture;
        }
        return null;
    }
}
