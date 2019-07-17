package httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.example.http.helloworld.HttpHelloWorldServerHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    //暂时不写ssl
    //   private final SslContext sslCtx;

    public HttpServerInitializer() {
        //  this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        //if (sslCtx != null) {
        //   p.addLast(sslCtx.newHandler(ch.alloc()));
        //}
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024)); //在处理 POST消息体时需要加上  ***很重要
        p.addLast(new HttpServerHandler());
    }
}