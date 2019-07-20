package httpserver;

import httpserver.util.RequestMappingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;

public class HttpNettyServer {

//    static final boolean SSL = System.getProperty("ssl") != null;

    public static void main(String[] args) throws CertificateException, InterruptedException, IllegalAccessException, InvocationTargetException, InstantiationException {
        int PORT = 8080;
        // Configure the server.

        // Configure SSL.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            RequestMappingUtil.init("httpserver.controller");
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to "
                    + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
