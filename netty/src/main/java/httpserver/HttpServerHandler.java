package httpserver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import httpserver.handle.RequestHandler;
import httpserver.model.entry.TestEntry;
import httpserver.util.RequestMappingUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject>
{



    //打印输入的内容
    private void printDetail(HttpRequest req) {
        System.out.println("uri:" + req.getUri());
        System.out.println("headers:" + req.headers().toString());
        System.out.println("method:" + req.method().toString());
    }

    //handle处理
    private void handleAction(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        // 识别路径
        // 寻找handle
        // 识别header,body,
        // handle做出回应

        //todo 可以启用线程池，异步处理逻辑部分，完成后只要执行ChannelFutureListener.CLOSE就能通知到阻塞调用方，完成调用过程
        if (acceptInboundMessage(msg)) {

            System.out.println("actionhandle");
            boolean release = true;
            try {
                release = false;
                executeChannelRead1(ctx, (HttpRequest) msg);
            } finally {
                if (release) {
                    ReferenceCountUtil.release(msg);
                }
            }
        }


        return;


    }

    //代码备份
    public void tmp() {
//        request = msg;
//
//        headers = request.headers();
//        String uri = request.uri();
//        HttpMethod method = request.method();
//        printDetail(request);
//        if (method.equals(HttpMethod.GET)) {
//            //Charsets.toCharset(CharEncoding.UTF_8)
//            QueryStringDecoder queryDecoder = new QueryStringDecoder(uri);
//            Map<String, List<String>> uriAttributes = queryDecoder.parameters();
//            //此处仅打印请求参数（你可以根据业务需求自定义处理）
//            for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
//                for (String attrVal : attr.getValue()) {
//                    logger.info(attr.getKey() + "=" + attrVal);
//
//                }
//            }
//            //  user.setMethod("get");
//        } else if (method.equals(HttpMethod.POST)) {
//            //POST请求,由于你需要从消息体中获取数据,因此有必要把msg转换成FullHttpRequest
//            fullRequest = (FullHttpRequest) msg;
//            String url = ((FullHttpRequest) msg).getUri();
//            //RequestHandler handler = RequestMappingUtil.getHandler();
//            RequestHandler handler = RequestMappingUtil.methodMap.get(url);
//            //handler.setHandlerObj(handler);
//
//            try {
//                handler.handle(ctx, fullRequest);//执行handle
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//        }
    }

    private void executeChannelRead1(final ChannelHandlerContext ctx, final HttpRequest msg) throws Exception {

        String json = ((FullHttpRequest) msg).content().toString(CharsetUtil.UTF_8);
        System.out.println("before runable:" + json);

        //ReferenceCountUtil.release(msg);
        String json2 = ((FullHttpRequest) msg).content().toString(CharsetUtil.UTF_8);
        System.out.println("before runable2:" + json2);
        final String tmp = "111";


        HttpNettyServer.executorGroup.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    String json = ((FullHttpRequest) msg).content().toString(CharsetUtil.UTF_8);
                    System.out.println("after runable:" + json);
                    System.out.println("after tmp:" + tmp);
                } finally {
                    ReferenceCountUtil.release(msg);
                }

            }
        });
        // Thread.sleep(2000);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // ctx.flush();
    }

    private static final String FAVICON_ICO = "/favicon.ico";
    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

    private HttpHeaders headers;
    private HttpRequest request;
    private FullHttpRequest fullRequest;


    Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            headers = request.headers();
            String uri = request.uri();
            //   logger.info("http uri: " + uri);
            if (uri.equals(FAVICON_ICO)) {
                return;
            }

            handleAction(ctx, msg);

        }

    }

    //返回实体
    public void testRespone(ChannelHandlerContext ctx) {
        String content = "{\"a\":\"val1\"}";

        TestEntry e = new TestEntry("a", "b");
        //        // 1.设置响应
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(JSONObject.toJSONString(e), CharsetUtil.UTF_8));

        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        //ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);//写完后一定要关闭channel后，服务端才会返回
        //JSONObject.toJSONString(new TestEntry("a11", "b11"))
        ctx.write(resp);
        // ctx.write(Unpooled.copiedBuffer(JSONObject.toJSONString(e), CharsetUtil.UTF_8));
        // ctx.write(Unpooled.copiedBuffer("This is response", CharsetUtil.UTF_8));

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        future.addListener(ChannelFutureListener.CLOSE);
        //ctx.write(resp);
    }

    public void testResponeEntry(ChannelHandlerContext ctx, HttpObject msg) {
        TestEntry e = new TestEntry("a", "b");
        //HttpVersion.HTTP_1_1
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        //response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.write(response);
        ctx.write(Unpooled.copiedBuffer(JSONObject.toJSONString(e), CharsetUtil.UTF_8));//最后记得flush

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        future.addListener(ChannelFutureListener.CLOSE);


    }

    /**
     * 简单处理常用几种 Content-Type 的 POST 内容（可自行扩展）
     *
     * @throws Exception
     */
    private void dealWithContentType() throws Exception {


        String contentType = getContentType();
        //可以使用HttpJsonDecoder
        if (contentType.equals("application/json")) {
            String jsonStr = fullRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
            // String jsonStr = fullRequest.content().toString();
            logger.info("jsonStr:" + jsonStr);

            JSONObject obj = JSON.parseObject(jsonStr);
//            for (Map.Entry<String, Object> item : obj.entrySet()) {
//                logger.info(item.getKey() + "=" + item.getValue().toString());
//            }

        } else if (contentType.equals("application/x-www-form-urlencoded")) {
            //方式一：使用 QueryStringDecoder
            String jsonStr = fullRequest.content().toString();
            QueryStringDecoder queryDecoder = new QueryStringDecoder(jsonStr, false);
            Map<String, List<String>> uriAttributes = queryDecoder.parameters();
            for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                for (String attrVal : attr.getValue()) {
                    logger.info(attr.getKey() + "=" + attrVal);
                }
            }

        } else if (contentType.equals("multipart/form-data")) {
            //TODO 用于文件上传
        } else {
            //do nothing...
        }
    }

    private String getContentType() {
        String typeStr = headers.get("Content-Type").toString();
        String[] list = typeStr.split(";");
        return list[0];
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }


}