package com.train.client.test;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 1、Client向Server发送消息：Are you ok?
 * 2、Server接收客户端发送的消息，并打印出来。
 * 3、Server端向客户端发送消息：I am ok!
 * 4、Client接收Server端发送的消息，并打印出来，通讯结束。
 */

public class HelloClient {

    StringBuffer message = new StringBuffer();

    public String connect(String host, int port) throws Exception {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
//            b.option(ChannelOption.AUTO_READ, false);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new HelloClientIntHandler(message));
                }
            });

            // Start the client.
            /**
             * await()方法：Waits for this future to be completed.
             * Waits for this future until it is done, and rethrows the cause of the failure if this future
             * failed.
             */
            ChannelFuture f = b.connect(host, port).channel().closeFuture().await();
            return message.toString();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        HelloClient client = new HelloClient();
        System.out.println(">>>>>>>>>>"+client.connect("127.0.0.1", 9090).toString());
    }
}