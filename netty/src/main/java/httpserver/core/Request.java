package httpserver.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface Request<T> {
    public Object attr(String key);

    public void attr(String key, Object value);

    public String getHeader(String name);

    public Object getParam(String key);

    public T getData();

    public ChannelHandlerContext getChannelHandlerContext();

    public FullHttpRequest getFullHttpRequest();

}
