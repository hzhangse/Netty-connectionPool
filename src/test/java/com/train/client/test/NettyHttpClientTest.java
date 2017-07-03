
package com.train.client.test;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.changcai.netty.client.NettyHttpClient;
import com.changcai.netty.client.NettyHttpRequest;
import com.changcai.netty.client.NettyHttpResponse;
import com.changcai.netty.client.NettyHttpResponseFuture;


public class NettyHttpClientTest {

    @Test
    public void testGet() throws Exception {
        final String url = "http://www.baidu.com:80";

        Map<String, Integer> maxPerRoute = new HashMap<String, Integer>();
        maxPerRoute.put("www.baidu.com:80", 100);

        final NettyHttpClient client = new NettyHttpClient.ConfigBuilder()
            .maxIdleTimeInMilliSecondes(200 * 1000).maxPerRoute(maxPerRoute)
            .connectTimeOutInMilliSecondes(30 * 1000).build();

        final NettyHttpRequest request = new NettyHttpRequest();
        request.header(HttpHeaders.Names.CONTENT_TYPE.toString(), "text/json; charset=GBK").uri(url);

        NettyHttpResponseFuture responseFuture = client.doGet(request);
        NettyHttpResponse response = (NettyHttpResponse) responseFuture.get();
        client.close();

        print(response);
    }

    //@Test
    public void testPost() throws Exception {
        final String postUrl = "http://www.xxx.com:8080/testPost";
        final String postContent = "";// json format

        Map<String, Integer> maxPerRoute = new HashMap<String, Integer>();
        maxPerRoute.put("www.xxx.com:80", 100);

        final NettyHttpClient client = new NettyHttpClient.ConfigBuilder()
            .maxIdleTimeInMilliSecondes(200 * 1000).maxPerRoute(maxPerRoute)
            .connectTimeOutInMilliSecondes(30 * 1000).build();

        final NettyHttpRequest request = new NettyHttpRequest();
        request.header(HttpHeaders.Names.CONTENT_TYPE.toString(), "text/json; charset=GBK").uri(postUrl)
            .content(postContent, null);

        NettyHttpResponseFuture responseFuture = client.doPost(request);
        NettyHttpResponse response = (NettyHttpResponse) responseFuture.get();
        client.close();
        print(response);
    }

    private void print(NettyHttpResponse response) {
        System.out.println("STATUS: " + response.getStatus());
        System.out.println("VERSION: " + response.getVersion());
        System.out.println();

        if (!response.getHeaders().isEmpty()) {
            for (String name : response.getHeaders().names()) {
                for (String value : response.getHeaders().getAll(name)) {
                    System.out.println("HEADER: " + name + " = " + value);
                }
            }
        }
        System.out.println("CHUNKED CONTENT :");
        for (ByteBuf buf : response.getContents()) {
            System.out.print(buf.toString(CharsetUtil.UTF_8));
        }
    }
}
