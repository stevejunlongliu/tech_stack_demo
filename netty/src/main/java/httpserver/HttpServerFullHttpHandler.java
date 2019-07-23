package httpserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.echo.EchoClient;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class HttpServerFullHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements ChannelHandler {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        if (acceptInboundMessage(fullHttpRequest)) {
            FullHttpRequest imsg = (FullHttpRequest) fullHttpRequest;
            boolean release = true;
            try {
                release = false;
                executeChannelRead(channelHandlerContext, imsg);
            } finally {
                if (release) {
                    ReferenceCountUtil.release(fullHttpRequest);
                }
            }
        }


        return;

    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //   System.out.println("aaa");
        ctx.flush();
    }

    private void executeChannelRead(final ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {

        String json = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("before runable:" + json);

        //ReferenceCountUtil.release(msg);
        final String json2 = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("before runable2:" + json2);
        final String tmp = "111";
        System.out.println("boss tmp:" + tmp);

        HttpNettyServer.executorGroup.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    System.out.println("isReadable：" + msg.content().isReadable());


                    msg.content().writeBytes(json2.getBytes());
                    System.out.println("after tmp:" + tmp);
                    String json = msg.content().toString(CharsetUtil.UTF_8);

                    //System.out.println("after runable:" + json2);

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
