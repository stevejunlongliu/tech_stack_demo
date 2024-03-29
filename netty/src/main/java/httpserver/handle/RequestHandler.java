package httpserver.handle;

import httpserver.annotation.RequestMethod;
import httpserver.annotation.ResponseDataTypeElement;
import httpserver.controller.TestController;
import httpserver.core.Request;
import httpserver.core.Response;
import httpserver.core.Result;
import httpserver.impl.RequestImpl;
import httpserver.impl.ResponseImpl;
import httpserver.util.HandlerUtil;
import httpserver.util.ThreadLocalLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

public class RequestHandler {
    private String url;

    private Class<?> handlerClass; //对应的controller

    private RequestMethod[] requestMethods;

    private boolean useGzip = true;//支持压缩


    private Method method;// 具体方法

    private int parameterOrder = 0;//todo 待确认注释

    private Object handlerObj;//handle实例

    private ResponseDataTypeElement responseContentType = ResponseDataTypeElement.JSON;

    private Class<?> dataType;

    public RequestHandler() {
    }

    public RequestHandler(String url, Object handlerObj, Method method) {
        this.url = url;
        this.handlerObj = handlerObj;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }

    public RequestMethod[] getRequestMethods() {
        return requestMethods;
    }

    public void setRequestMethods(RequestMethod[] requestMethods) {
        this.requestMethods = requestMethods;
    }

    public boolean isUseGzip() {
        return useGzip;
    }

    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public int getParameterOrder() {
        return parameterOrder;
    }

    public void setParameterOrder(int parameterOrder) {
        this.parameterOrder = parameterOrder;
    }

    public Object getHandlerObj() {
        return handlerObj;
    }

    public void setHandlerObj(Object handlerObj) {
        this.handlerObj = handlerObj;
    }

    public ResponseDataTypeElement getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(ResponseDataTypeElement responseContentType) {
        this.responseContentType = responseContentType;
    }


    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<?> handlerClass) {
        this.handlerClass = handlerClass;
    }

    //处理http请求
    public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        String httpMethodName = req.getMethod().name();
        RequestMethod httpMethod = RequestMethod.getEnum(httpMethodName);
        boolean allowRequest = false;//请求类型是否符合接口要求
        for (RequestMethod r : requestMethods) {
            if (r.equals(httpMethod)) {
                allowRequest = true;
                break;
            }
        }


        boolean keepAlive = HttpHeaders.isKeepAlive(req);
        if (!allowRequest) {

            HandlerUtil.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, keepAlive);
            return;
        }


        long begin = System.currentTimeMillis();
        RequestImpl<Object> request = new RequestImpl<Object>(ctx, req);//getParameterImpl();
        request.setDataType(getDataType());
        boolean gzip = useGzip;
        if (gzip) {
            String str = req.headers().get("accept-encoding");
            gzip = (str != null) && (str.contains("gzip"));
        }

        ThreadLocalLog.addLog("decode param cost:" + (System.currentTimeMillis() - begin));
        if (!keepAlive) {
            ThreadLocalLog.addLog("keep alive disable");
        }
        ResponseImpl response = new ResponseImpl(ctx, responseContentType, gzip, keepAlive);
        Object result = null;

        // 根据请求路由，选择对应的handle

        switch (parameterOrder) {//根据参数类型，选择invoke的方式
            case 0:
                //result = method.invoke(handlerObj);
                result = method.invoke(handlerObj);
                break;
            case 1:
                //result = method.invoke(handlerObj, request);
                result = method.invoke(handlerObj, request);
                break;
        }

        if (result != null) {
            Class<?> clz = result.getClass();
            if (clz == byte[].class) {
                response.write((byte[]) result);
            } else if (clz == String.class) {
                response.write((String) result);
            } else {
                if (responseContentType == ResponseDataTypeElement.JSON) {
                    if (result instanceof Result) {
                        setHttpStatus((Result) result, response);
                    }
                    response.writeJson(result);
                }
//                else if (responseContentType == ResponseDataTypeElement.File) {
//                    if (result instanceof File) {
//                        response.write((File) result);
//                    } else if (result instanceof Result) {
//                        response.setStatus(HttpResponseStatus.NOT_FOUND);
//                        response.setContentType("text/plain; charset=utf-8");
//                        response.write(((Result) result).getError());
//                    }
//                } else if (responseContentType == ResponseDataTypeElement.TEXT) {
//                    if (result instanceof Result) {
//                        response.setContentType("text/plain; charset=utf-8");
//                        if (((Result) result).isSuccess()) {
//                            response.write("" + ((Result) result).getData());
//                        } else {
//                            setHttpStatus((Result) result, response);
//                            response.write(((Result) result).getError());
//                        }
//                    } else {
//                        response.write(result.toString());
//                    }
//                } else {
//                    response.write(result.toString());
//                }
            }
        }
        response.close();


    }

    private void setHttpStatus(Result result, Response response) {
        if (result.isSuccess()) return;
        int errorCode = result.getErrorCode();
        int httpStatusCode = errorCode / 100;
        if (httpStatusCode >= 400 && httpStatusCode < 600) {
            HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(httpStatusCode);
            if (!StringUtils.contains(httpResponseStatus.reasonPhrase(), "(" + httpStatusCode + ")")) {
                response.setStatus(httpResponseStatus);
            }
        }
    }

    public void setParameter(Class<?>[] parameter, int parameterOrder) {
//        this.parameter = parameter;
        this.parameterOrder = parameterOrder;
    }

    public static int genParameterOrder(Class<?>[] parameter) {
        if (parameter == null || parameter.length == 0)
            return 0;
        else if (parameter.length == 1) {
            if (Request.class.equals(parameter[0])) {
                return 1;
            } else if (Response.class.equals(parameter[0])) {
                return 2;
            }
        } else if (parameter.length == 2) {
            if (Request.class.equals(parameter[0]) && Response.class.equals(parameter[1])) {
                return 3;
            } else if (Response.class.equals(parameter[1]) && Response.class.equals(parameter[2])) {
                return 4;
            }
        }
        return -1;
    }

}
