package httpserver.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import httpserver.annotation.ResponseDataTypeElement;
import httpserver.core.Response;
import httpserver.model.entry.TestEntry;
import httpserver.util.FileUtil;
import httpserver.util.HandlerUtil;
import httpserver.util.ThreadLocalLog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;


public class ResponseImpl implements Response {

    private ChannelHandlerContext ctx;
    private HttpResponse __response;
    private AtomicBoolean hasWrite = new AtomicBoolean(false);
    private AtomicBoolean useGzip = new AtomicBoolean(true);
    private boolean isCanKeepAlive = false;
    private boolean isKeepAlive = false;
    //ByteBuf reslutByte;
    private ByteBufOutputStream bbos = null;
    private GZIPOutputStream gzipStream = null;
    private File file = null;
    private AtomicBoolean hasSetContentType = new AtomicBoolean(false);
    private AtomicBoolean close = new AtomicBoolean(false);

    public ResponseImpl(ChannelHandlerContext ctx, ResponseDataTypeElement responseContentType, boolean useGzip, boolean isKeepAlive) {
        this.ctx = ctx;
        __response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);//new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        //__response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);//new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        //HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);//new 的时候是full
//        __response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
        if (responseContentType == ResponseDataTypeElement.TEXT) {
            setContentType("text/plain; charset=utf-8");
        } else if (responseContentType == ResponseDataTypeElement.HTML) {
            setContentType("text/html; charset=utf-8");
        } else if (responseContentType == ResponseDataTypeElement.JSON) {
            setContentType("application/json; charset=utf-8");
        } else if (responseContentType == ResponseDataTypeElement.File) {
            setContentType("text/plain; charset=utf-8");
        }
        this.useGzip.set(useGzip);
        this.isCanKeepAlive = isKeepAlive;
        this.isKeepAlive = isKeepAlive;
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        String type = FileUtil.getMimeType(file);
        response.headers().set(CONTENT_TYPE, type);
    }

    // @Override
    public void setStatus(HttpResponseStatus status) {
        __response.setStatus(status);
    }

    // @Override
    public void setContentType(String contentType) {
        hasSetContentType.set(true);
        setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
    }

    //  @Override
    public void setHeader(String name, Object value) {
        if (HttpHeaders.Names.CONNECTION.equals(name)) {
            isKeepAlive = HttpHeaders.Values.KEEP_ALIVE.equals(value);
        }
        if (value instanceof Date) {
            HttpHeaders.setDateHeader(__response, name, (Date) value);
        } else {
            HttpHeaders.setHeader(__response, name, value);
        }
    }

    private GZIPOutputStream getGzipStream() throws Exception {
        if (this.bbos == null || this.gzipStream == null) {
            IOUtils.closeQuietly(this.bbos);
            if (this.bbos != null) {
                ByteBuf buffer = this.bbos.buffer();
                ReferenceCountUtil.release(buffer);
            }
            this.bbos = new ByteBufOutputStream(Unpooled.directBuffer());
            this.gzipStream = new GZIPOutputStream(this.bbos);
        }
        return this.gzipStream;
    }

    private ByteBufOutputStream getByteBufOutputStream() throws Exception {
        if (this.gzipStream != null) {
            throw new Exception("gzip is not null");
        }
        if (this.bbos == null) {
            this.bbos = new ByteBufOutputStream(Unpooled.directBuffer());
        }
        return this.bbos;
    }

    public Response writeJson(Object msg) throws Exception {
        long begin = System.currentTimeMillis();
        //
        //reslutByte = Unpooled.copiedBuffer(JSONObject.toJSONString(msg), CharsetUtil.UTF_8);

        OutputStreamWriter osw = new OutputStreamWriter((useGzip.get() ? getGzipStream() : getByteBufOutputStream()), CharsetUtil.UTF_8);
        writeJSONString(msg, osw);
        osw.flush();
        long t = System.currentTimeMillis() - begin;
        if (t > 10) {
            ThreadLocalLog.addLog("--", "Response.writeJson:" + t);
        }
        return this;
    }

    private void writeJSONString(Object object, Writer writer) {
        SerializeWriter out = new SerializeWriter(writer);
        try {
            JSONSerializer serializer = new JSONSerializer(out);
            serializer.config(SerializerFeature.BrowserCompatible, true);
            // serializer.getPropertyFilters().add(ResponseDataFilter.getInstance());
            serializer.write(object);
        } finally {
            out.close();
        }
    }

    //ssssss @Override
    public Response write(String msg) throws Exception {
        long begin = System.currentTimeMillis();
        byte[] msgBytes = msg.getBytes();//CharsetUtil.UTF_8
        if (useGzip.get()) {
            getGzipStream().write(msgBytes);
        } else {
            getByteBufOutputStream().write(msgBytes);
        }
        //  getByteBufOutputStream().write(msgBytes);
        long t = System.currentTimeMillis() - begin;
        if (t > 10) {
            ThreadLocalLog.addLog("--", "Response.write string:" + t);
        }
        return this;
    }

    public Response write(File file) throws Exception {
        if (this.bbos != null || this.gzipStream != null || hasWrite.get()) {
            throw new Exception("error write file");
        }
        this.file = file;
        return this;
    }

    // @Override
    public Response write(byte[] msg) throws Exception {
        return write(msg, 0, msg.length);
    }

    //@Override
    public Response write(byte[] msg, int off, int len) throws Exception {
        long begin = System.currentTimeMillis();
        if (useGzip.get()) {
            getGzipStream().write(msg, off, len);
        } else {
            getByteBufOutputStream().write(msg, off, len);
        }
        long t = System.currentTimeMillis() - begin;
        if (t > 10) {
            ThreadLocalLog.addLog("--", "Response.write byte:" + t);
        }
        return this;
    }

    // @Override
    public Response flush() throws Exception {
//        if (file == null) {
//            if (useGzip.get()) {
//
//                if (gzipStream != null) gzipStream.flush();
//            } else {
//                if (bbos != null) {
//                    bbos.flush();
//                    ByteBuf buffer = this.bbos.buffer();
//                    IOUtils.closeQuietly(this.bbos);
//                    this.bbos = null;
//                    if (!hasWrite.getAndSet(true)) {
//                        if (HttpHeaders.getHeader(__response, HttpHeaders.Names.CONNECTION) == null) {
//                            setHeaderConnection();
//                        }
//                        ctx.write(__response);
//                    }
//                    ctx.write(buffer);
//                }
//            }
//        }
        ctx.write(__response);
        return this;
    }

    public boolean isClose() {
        return close.get();
    }

    //对返回结果进行写入
    public void close() throws Exception {



        if (close.getAndSet(true)) {
            throw new IllegalStateException("closed");
        }
        long begin = System.currentTimeMillis();
        if (this.gzipStream != null) {
            this.gzipStream.finish();
            IOUtils.closeQuietly(this.gzipStream);
            this.gzipStream = null;
        }
        if (this.bbos != null) {

            //test
//            TestEntry eee = new TestEntry("a", "b");
//            ByteBuf byteBufTest = Unpooled.copiedBuffer(JSONObject.toJSONString(eee), CharsetUtil.UTF_8);
            //

            ByteBuf buffer = this.bbos.buffer();
            IOUtils.closeQuietly(this.bbos);
            this.bbos = null;
            if (!hasWrite.getAndSet(true)) {
                setHeaderConnection();
                HttpHeaders.setHeader(__response, HttpHeaders.Names.CONTENT_LENGTH, Integer.toString(buffer.readableBytes()));
                //HttpHeaders.setHeader(__response, HttpHeaders.Names.CONTENT_LENGTH, Integer.toString(byteBufTest.readableBytes()));
                if (useGzip.get()) {
                    HttpHeaders.setHeader(__response, HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.GZIP);
                }
                ctx.write(__response);
            }

            //ctx.write(byteBufTest);//最后记得flush
            ctx.write(buffer);
        } else if (file != null) {
            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                HandlerUtil.sendError(ctx, HttpResponseStatus.NOT_FOUND, isKeepAlive && isCanKeepAlive);
                return;
            }
            long fileLength = raf.length();

            if (!hasWrite.getAndSet(true)) {
                if (!hasSetContentType.get()) setContentTypeHeader(__response, file);
                setHeaderConnection();
                HttpHeaders.setHeader(__response, HttpHeaders.Names.CONTENT_LENGTH, Long.toString(fileLength));
                ctx.write(__response);
            }

            ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(raf, 0, fileLength, 8192), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                }
            });
        } else if (!hasWrite.getAndSet(true)) {
            ctx.write(__response);
        }


        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
         if (!(isKeepAlive && isCanKeepAlive)) {
        lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
        ThreadLocalLog.addLog("--", "Response.close:" + (System.currentTimeMillis() - begin));
    }

    public String convertByteBufToString(ByteBuf buf) {
        String str;
        if (buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }


    private void setHeaderConnection() {
        if (HttpHeaders.getHeader(__response, HttpHeaders.Names.CONNECTION) == null) {
            if (isKeepAlive && isCanKeepAlive) {
                HttpHeaders.setHeader(__response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            } else {
                HttpHeaders.setHeader(__response, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            }
        }
    }

}
