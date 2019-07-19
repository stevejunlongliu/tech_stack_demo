package httpserver.core;

import io.netty.handler.codec.http.HttpResponseStatus;


public interface Response {
    public void setStatus(HttpResponseStatus status);

    public void setContentType(String contentType);

    public void setHeader(String name, Object value);

    public Response write(String msg) throws Exception;

    public Response write(byte[] msg) throws Exception;

    public Response write(byte[] msg, int off, int len) throws Exception;

    public Response flush() throws Exception;

    public boolean isClose();

    public void close() throws Exception;

}
