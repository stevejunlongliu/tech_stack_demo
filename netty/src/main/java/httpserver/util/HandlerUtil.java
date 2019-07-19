package httpserver.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Map;


public class HandlerUtil {
    public final static String CURRENT_3GNO_KEY = "__CURRENT_3G_NO__";
    public final static String CURRENT_USER_ID = "__CURRENT_USER_ID__";
    public final static String CURRENT_USER_STATUS = "__CURRENT_USER_STATUS__";
    public final static String CURRENT_USER_NAME = "__CURRENT_USER_NAME__";
    public final static String CURRENT_REQUEST_ID = "__CURRENT_REQUEST_ID__";
    public static final String CLIENT_ID = "__EC_LITE_CLIENT_ID__";
    public static final String CLIENT_IP = "__EC_LITE_CLIENT_IP__";
    public static final String CLIENT_VERSION = "__EC_LITE_CLIENT_VERSION__";
    public static final String CLIENT_OS_VERSION = "__EC_LITE_CLIENT_OS_VERSION__";
    public static final String CLIENT_DEVICE_ID = "__EC_LITE_CLIENT_DEVICE_ID__";
    public static final String CLIENT_DEVICE_NAME = "__EC_LITE_CLIENT_DEVICE_NAME__";
    public static final String REQUEST_START_TIME = "__EC_LITE_REQUEST_START_TIME__";
    public static final String AUTH_TOKEN_COST = "__EC_LITE_AUTH_TOKEN_COST__";

    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, boolean isKeepAlive) {
        sendError(ctx, status, isKeepAlive, null);
    }

    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, boolean isKeepAlive, Map<String, String> headers) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(byteBuf.readableBytes()));
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                response.headers().set(entry.getKey(), entry.getValue());
            }
        }
        if (isKeepAlive) {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            HttpHeaders.setHeader(response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }
        ChannelFuture future = ctx.writeAndFlush(response);
        if (!isKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
