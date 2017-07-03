package com.train.client.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


//InboundHandler类型
public class HelloClientIntHandler extends ChannelInboundHandlerAdapter {

    public StringBuffer message;

    public HelloClientIntHandler(StringBuffer message) {
        this.message = message;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("==============channel--register==============");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("==============channel--unregistered==============");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("==============channel--inactive==============");
    }

    // 连接成功后，向server发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("==============channel--active==============");
        String msg = "Are you ok?";
        /**
         * 分配ByteBuf
         * Return the assigned {@link io.netty.buffer.ByteBufAllocator} which will be used to allocate {@link ByteBuf}s.
         */
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.write(encoded);
        Thread.sleep(10000);
        ctx.flush();
    }

    // 接收server端的消息，并打印出来
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("==============channel--read==============");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        System.out.println("Server said:" + new String(result1));
        message.append(new String(result1));
        result.release();
    }
}