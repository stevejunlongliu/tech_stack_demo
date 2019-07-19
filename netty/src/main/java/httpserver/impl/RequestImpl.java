package httpserver.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import httpserver.core.Request;
import httpserver.util.AcceptLanguageParser;
import httpserver.util.HandlerUtil;
import httpserver.util.StringMap;
import httpserver.util.ThreadLocalLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class RequestImpl<T> implements Request<T> {
    private static final Logger logger = LoggerFactory.getLogger(RequestImpl.class);
    private Map<String, Object> attr;
    private Map<String, Object> params;
    private Object data = null;
    private Class<?> dataType = null;
    private HttpHeaders headers;
    private File tmpUploadFile = null;
    private boolean isParamOk = true;
    private FullHttpRequest req;
    private ChannelHandlerContext ctx;
    //private Locale currentLang = I18NBO.getDefaultLocale();

    public RequestImpl(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        attr = new HashMap<String, Object>();
        params = new StringMap<Object>();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.getUri());
        params.put("currentUrl", queryStringDecoder.path());
        params.put("current3gNo", req.headers().get(HandlerUtil.CURRENT_3GNO_KEY));
        params.put("currentUserId", req.headers().get(HandlerUtil.CURRENT_USER_ID));
        params.put("currentUserName", req.headers().get(HandlerUtil.CURRENT_USER_NAME));
        params.put("currentClientIP", req.headers().get(HandlerUtil.CLIENT_IP));
        decodeQueryString(queryStringDecoder.parameters());
        this.headers = req.headers();
        isParamOk = true;
        if (req.getMethod() == HttpMethod.POST) {
            decodePostData(req);
        }
        clientVersion(req);
        // decodeLang(req, req.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE.toString()));
        this.ctx = ctx;
        this.req = req;
    }


    private void decodeLang(FullHttpRequest req, String langHeader) {
        //业务类 不管了
//        Locale locale = AcceptLanguageParser.getLocale(langHeader, I18NBO.getDefaultLocale());
//        if (req.headers() != null) {
//            String clientId = req.headers().get(HandlerUtil.CLIENT_ID);
//            ClientBean clientBean = ClientBO.getInstance().getClientBean(clientId);
//            if (clientBean != null && ClientTypeEnum.desktop == ClientTypeEnum.getEnum(clientBean.getClientType())) {
//                locale = I18NBO.getDefaultLocale();
//            }
//        }
//        if (locale != null) {
//            currentLang = locale;
//            params.put("currentLang", currentLang);
//        }
    }

    private static String adjustContentType(String sb) {
        String str = sb.toLowerCase();
        if (!str.contains("multipart/form-data"))
            return sb;
        String[] split = sb.split(";");
        str = "";
        for (String s : split) {
            String s1 = s.trim().toLowerCase();
            if (s1.startsWith("multipart/form-data")
                    || s1.startsWith("boundary=")) {
                if (str.length() > 0)
                    str += "; ";
                str += s;
            }
        }
        return str;
    }

    private void decodeQueryString(Map<String, List<String>> params) throws Exception {
        if (params == null || params.isEmpty()) return;
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String name = entry.getKey();
            List<String> value = entry.getValue();
            int size = value.size();
            if (size == 1) {
                this.params.put(name, value.get(0));
            } else if (size > 1) {
                this.params.put(name, value);
            }
        }
    }

    private void decodePostData(FullHttpRequest req) throws Exception {
        String contentType = req.headers().get("Content-type");

        if (!StringUtils.isEmpty(contentType)) {
            String lowerCaseContentType = contentType.toLowerCase();
            if (lowerCaseContentType.contains("application/json")) {
                decodePostJSONData(req);
            } else {
                String adjustContentType = adjustContentType(contentType);
                req.headers().set("Content-type", adjustContentType);
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE), req, CharsetUtil.UTF_8);
                List<InterfaceHttpData> dataList = decoder.getBodyHttpDatas();
                for (InterfaceHttpData data : dataList) {
                    String name = data.getName();
                    Object value = null;
                    if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                        MixedAttribute attribute = (MixedAttribute) data;
                        attribute.setCharset(CharsetUtil.UTF_8);
                        value = attribute.getValue();
                    } else if (InterfaceHttpData.HttpDataType.FileUpload == data.getHttpDataType()) {
                        FileUpload fileUpload = (FileUpload) data;
                        if (fileUpload.isInMemory()) {
                            byte[] fileData = fileUpload.get();
                            tmpUploadFile = File.createTempFile("xt_", ".tmp");
                            FileOutputStream fos = new FileOutputStream(tmpUploadFile);
                            try {
                                fos.write(fileData);
                            } catch (Throwable ignore) {
                            } finally {
                                fos.close();
                            }
                            value = tmpUploadFile;
                        } else {
                            value = fileUpload.getFile();
                        }
                    } else if (InterfaceHttpData.HttpDataType.InternalAttribute == data.getHttpDataType()) {
                        // TODO 未处理
                        value = null;
                    }
                    putParam(name, value);
                }
            }
        }
    }

    private void putParam(String name, Object value) {
        if (params.containsKey(name)) {
            Object oldVal = params.get(name);
            if (oldVal instanceof List) {
                //noinspection unchecked
                ((List) oldVal).add(value);
            } else {
                List<Object> l = new ArrayList<Object>();
                l.add(oldVal);
                l.add(value);
                params.put(name, l);
            }
        } else {
            params.put(name, value);
        }
    }

    private void decodePostJSONData(FullHttpRequest req) throws Exception {
        String json = req.content().toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(json)) return;
        try {
            StringMap data = JSON.parseObject(json, StringMap.class);
            //noinspection unchecked
            params.putAll(data);
        } catch (Throwable t) {
            isParamOk = false;
            logger.error("error json data : " + json + "\nrequest info:" + JSON.toJSONString(params), t);
        }
    }

    private void clientVersion(FullHttpRequest request) {
        String userAgent = (String) params.get("ua");
        if (org.apache.commons.lang3.StringUtils.isEmpty(userAgent)) {
            userAgent = request.headers().get("User-Agent");
        }
        String[] infos = null;
        try {
            infos = UserAgent.parser(userAgent);
        } catch (Throwable ignore) {
            logger.error("user agent parse error [" + userAgent + "]", ignore);
        }
        if (infos != null && infos.length == 5) {
            headers.set(HandlerUtil.CLIENT_ID, infos[0]);
            headers.set(HandlerUtil.CLIENT_VERSION, infos[1]);
            params.put("currentClientId", infos[0]);
            params.put("currentClientVersion", infos[1]);

            if (!StringUtils.isEmpty(infos[2])) {
                headers.set(HandlerUtil.CLIENT_DEVICE_ID, infos[2]);
                params.put("currentClientDeviceId", infos[2]);
            }
            if (StringUtils.isNotEmpty(infos[3])) {
                headers.set(HandlerUtil.CLIENT_DEVICE_NAME, infos[3]);
                params.put("currentClientDeviceName", infos[3]);
            }

//            if (StringUtils.isNotEmpty(infos[4])
//                    && EcLiteUtil.isMobileClientId(infos[0])) {
//                headers.set(HandlerUtil.CLIENT_OS_VERSION, infos[4]);
//                params.put("currentClientOSVersion", infos[4]);
//            }
        }
    }

    public void attr(String key, Object value) {
        attr.put(key, value);
    }

    public Object attr(String key) {
        return attr.get(key);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public T getData() {


        // data = JSONObject.parseObject(params.toString(), dataType);

        if (data == null && dataType != null) {
            if (StringMap.class == dataType) {
                data = params;
            } else {
                long begin = System.currentTimeMillis();
                try {
                    //todo 原方法不生效，回头看看为什么
                    data = map2Object(params, dataType);
                    //data = RequestDataConverter.getInstance().convert(params, dataType);
                } catch (Throwable t) {
                    logger.error("covert data error", t);
                } finally {
                    ThreadLocalLog.addLog("covert request cost:" + (System.currentTimeMillis() - begin));
                }
                begin = System.currentTimeMillis();
                try {
                    RequestDataFilter.getInstance().filter(data);
                } catch (Throwable t) {
                    logger.error("filter data error", t);
                } finally {
                    ThreadLocalLog.addLog("filter data cost:" + (System.currentTimeMillis() - begin));
                }

            }
        }
        //noinspection unchecked
        return (T) data;
    }


    /**
     * Map转成实体对象
     *
     * @param map   map实体对象包含属性
     * @param clazz 实体对象类型
     * @return
     */
    public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        T obj = null;
        try {
            obj = clazz.newInstance();

            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                String filedTypeName = field.getType().getName();
                if (filedTypeName.equalsIgnoreCase("java.util.date")) {
                    String datetimestamp = String.valueOf(map.get(field.getName()));
                    if (datetimestamp.equalsIgnoreCase("null")) {
                        field.set(obj, null);
                    } else {
                        field.set(obj, new Date(Long.parseLong(datetimestamp)));
                    }
                } else {
                    field.set(obj, map.get(field.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void cleanTmpUploadFile() {
//        if (tmpUploadFile != null) {
//            tmpUploadFile.delete();
//        }
    }

    public boolean isParamOk() {
        return isParamOk;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return this.ctx;
    }

    public FullHttpRequest getFullHttpRequest() {
        return this.req;
    }

//    public Locale getCurrentLang() {
//        return currentLang;
//    }
}
