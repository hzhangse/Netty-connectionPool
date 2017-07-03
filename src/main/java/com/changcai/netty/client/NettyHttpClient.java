/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.client;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.changcai.netty.handler.AdditionalChannelInitializer;
import com.changcai.netty.pool.NettyHttpChannelPool;
import com.changcai.netty.util.NettyHttpRequestUtil;

/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public class NettyHttpClient {

	private NettyHttpChannelPool channelPool;

	private ConfigBuilder configBuilder;

	private NettyHttpClient(ConfigBuilder configBuilder) {
		this.configBuilder = configBuilder;
		this.channelPool = new NettyHttpChannelPool(configBuilder.getMaxPerRoute(),
				configBuilder.getConnectTimeOutInMilliSecondes(),
				configBuilder.getMaxIdleTimeInMilliSecondes(),
				configBuilder.getForbidForceConnect(),
				configBuilder.getAdditionalChannelInitializer(),
				configBuilder.getOptions(), configBuilder.getGroup());
	}

	public NettyHttpResponseFuture doPost(NettyHttpRequest request)
			throws Exception {

		HttpRequest httpRequest = NettyHttpRequestUtil.create(request,
				HttpMethod.POST);
		InetSocketAddress route = new InetSocketAddress(request.getUri()
				.getHost(), request.getUri().getPort());

		return channelPool.sendRequest(route, httpRequest);
	}

	public NettyHttpResponseFuture doGet(NettyHttpRequest request)
			throws Exception {
		HttpRequest httpRequest = NettyHttpRequestUtil.create(request,
				HttpMethod.GET);
		InetSocketAddress route = new InetSocketAddress(request.getUri()
				.getHost(), request.getUri().getPort());
		return channelPool.sendRequest(route, httpRequest);
	}

	public void close() throws InterruptedException {
		channelPool.close();
	}

	public ConfigBuilder getConfigBuilder() {
		return configBuilder;
	}

	public void setConfigBuilder(ConfigBuilder configBuilder) {
		this.configBuilder = configBuilder;
	}

	public static final class ConfigBuilder {
		@SuppressWarnings("unchecked")
		private Map<ChannelOption, Object> options = new HashMap<ChannelOption, Object>();

		// max idle time for a channel before close
		private int maxIdleTimeInMilliSecondes;

		// max time wait for a channel return from pool
		private int connectTimeOutInMilliSecondes;

		/**
		 * value is false indicates that when there is not any channel in pool
		 * and no new channel allowed to be create based on maxPerRoute, a new
		 * channel will be forced to create.Otherwise, a
		 * <code>TimeoutException</code> will be thrown value is false.
		 */
		private boolean forbidForceConnect = false;

		private AdditionalChannelInitializer additionalChannelInitializer;

		// max number of channels allow to be created per route
		private Map<String, Integer> maxPerRoute;

		private EventLoopGroup customGroup;

		public ConfigBuilder() {
		}

		public NettyHttpClient build() {
			return new NettyHttpClient(this);
		}

		public ConfigBuilder maxPerRoute(Map<String, Integer> maxPerRoute) {
			this.maxPerRoute = maxPerRoute;
			return this;
		}

		public ConfigBuilder connectTimeOutInMilliSecondes(
				int connectTimeOutInMilliSecondes) {
			this.connectTimeOutInMilliSecondes = connectTimeOutInMilliSecondes;
			return this;
		}

		@SuppressWarnings("unchecked")
		public ConfigBuilder option(ChannelOption key, Object value) {
			options.put(key, value);
			return this;
		}

		public ConfigBuilder maxIdleTimeInMilliSecondes(
				int maxIdleTimeInMilliSecondes) {
			this.maxIdleTimeInMilliSecondes = maxIdleTimeInMilliSecondes;
			return this;
		}

		public ConfigBuilder additionalChannelInitializer(
				AdditionalChannelInitializer additionalChannelInitializer) {
			this.additionalChannelInitializer = additionalChannelInitializer;
			return this;
		}

		public ConfigBuilder customGroup(EventLoopGroup customGroup) {
			this.customGroup = customGroup;
			return this;
		}

		public ConfigBuilder forbidForceConnect(boolean forbidForceConnect) {
			this.forbidForceConnect = forbidForceConnect;
			return this;
		}

		@SuppressWarnings("unchecked")
		public Map<ChannelOption, Object> getOptions() {
			return options;
		}

		public int getMaxIdleTimeInMilliSecondes() {
			return maxIdleTimeInMilliSecondes;
		}

		public AdditionalChannelInitializer getAdditionalChannelInitializer() {
			return additionalChannelInitializer;
		}

		public Map<String, Integer> getMaxPerRoute() {
			return maxPerRoute;
		}

		public int getConnectTimeOutInMilliSecondes() {
			return connectTimeOutInMilliSecondes;
		}

		public EventLoopGroup getGroup() {
			return this.customGroup;
		}

		public boolean getForbidForceConnect() {
			return this.forbidForceConnect;
		}
	}
}
