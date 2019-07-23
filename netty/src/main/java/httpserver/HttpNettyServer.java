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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;

public class HttpNettyServer {

//    static final boolean SSL = System.getProperty("ssl") != null;

    //业务线程池- - 拆分后，能根据业务处理的能力，分配不同的线程数
    //线程数计算逻辑  nthread=(1+iot/cput)*cpun  当前测试iot为200ms，cput为4ms，cpun为2理论值应该为100
    //实际使用时应配合压测得到最优的线程数
    public static DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(64, new DefaultThreadFactory("Demo-ExecutorGroup"));

    //
    private static int boosThreadCount = 1;
    //工作线程 ，因将逻辑操作拆分到的业务线程池中异步执行，故将工作线程降为8
    private static int workThreadCount = 8;

    private static String compmentScan = "httpserver.controller";

    public static void main(String[] args) throws CertificateException, InterruptedException, IllegalAccessException, InvocationTargetException, InstantiationException {
        int PORT = 8080;
        // Configure the server.

        // Configure SSL.
        EventLoopGroup bossGroup = new NioEventLoopGroup(boosThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workThreadCount);
        try {
            RequestMappingUtil.init(compmentScan);
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
