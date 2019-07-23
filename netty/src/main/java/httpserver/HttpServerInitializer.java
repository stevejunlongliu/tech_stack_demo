package httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.example.http.helloworld.HttpHelloWorldServerHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    //暂时不写ssl
    //   private final SslContext sslCtx;

    public HttpServerInitializer() {
        //  this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        //if (sslCtx != null) {
        //   p.addLast(sslCtx.newHandler(ch.alloc()));
        //}
//        p.addLast(new HttpServerCodec());
//        p.addLast(new HttpServerExpectContinueHandler());
//        p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024)); //在处理 POST消息体时需要加上  ***很重要
        //p.addLast(new HttpServerHandler());HttpServerFullHttpHandler
        // p.addLast(new HttpServerFullHttpHandler());
        //  p.addLast(HttpNettyServer.executorGroup, "ttt", new HttpServerFullHttpHandler());
        pipeline.addLast("readTimeout", new ReadTimeoutHandler(1800)); // 30分钟
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(20971520));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        //pipeline.addLast(HttpNettyServer.executorGroup, "ttt", new HttpServerFullHttpHandler());
        pipeline.addLast(new HttpServerFullHttpAdapter());
    }
}