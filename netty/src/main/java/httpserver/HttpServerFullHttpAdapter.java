package httpserver;

import httpserver.handle.RequestHandler;
import httpserver.util.RequestMappingUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;


//继承ChannelInboundHandlerAdapter，当继承SimpleChannelInboundHandler时，
// 如果使用线程异步处理逻辑时，会因主线程不知什么地方对ctx进行了flush操作的，导致线程内无法读取到request的content内容
public class HttpServerFullHttpAdapter extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest imsg = (FullHttpRequest) msg;
        System.out.println("actionhandle");
        boolean release = true;
        try {
            release = false;
            executeChannelRead1(ctx, imsg);
        } finally {
            if (release) {
                ReferenceCountUtil.release(imsg);
            }
        }
    }




    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //System.out.println("aaa");
        ctx.flush();
    }

    private void executeChannelRead1(final ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {

        HttpNettyServer.executorGroup.execute(new Runnable() {
            @Override
            public void run() {
                try {


                    String json = msg.content().toString(CharsetUtil.UTF_8);

                    System.out.println("after runable:" + json);

                    FullHttpRequest fullRequest = (FullHttpRequest) msg;
                    String url = ((FullHttpRequest) msg).getUri();

                    RequestHandler handler = RequestMappingUtil.methodMap.get(url);

                    try {
                        handler.handle(ctx, fullRequest);//执行handle
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } finally {
                    ReferenceCountUtil.release(msg);
                }

            }
        });
        // Thread.sleep(2000);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {

    }
}
