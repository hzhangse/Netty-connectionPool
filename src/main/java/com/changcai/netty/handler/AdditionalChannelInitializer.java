/**
 * ChangCai.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.changcai.netty.handler;

import io.netty.channel.Channel;
/**
 * 
 * @author ryan
 * @version $Id: aaa.java, v 0.1 2016年9月6日 下午3:57:51 ryan Exp $
 */
public interface AdditionalChannelInitializer {

	void initChannel(Channel ch) throws Exception;
}
