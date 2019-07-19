package httpserver.util;

import httpserver.annotation.RequestMethod;
import httpserver.controller.TestController;
import httpserver.core.Request;
import httpserver.handle.RequestHandler;
import httpserver.impl.RequestImpl;
import httpserver.model.entry.TestEntry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import httpserver.annotation.RequestMapping;

public class RequestMappingUtil {
    private static final Logger logger = LoggerFactory.getLogger(RequestMappingUtil.class);

    public static RequestHandler getHandler() throws Exception {

        //todo 先搞定一个handler返回出去，执行到对应的的方法上
        Method method = TestController.class.getMethod("groupInfo", Request.class);
        RequestHandler mh = new RequestHandler();
        //mh.setHandlerObj(obj);
        mh.setHandlerClass(TestController.class);
        //mh.setMethod(TestController.class.getMethod("groupInfo"));
        mh.setMethod(method);
        //mh.setUrl(s);
        int parameterOrder = RequestHandler.genParameterOrder(method.getParameterTypes());
        if (parameterOrder < 0)
            return null;
        mh.setParameter(method.getParameterTypes(), parameterOrder);
        Class<?> dataTypeClz = getRequestDataType(mh.getMethod());
        mh.setDataType(dataTypeClz);
        RequestMethod[] reqMethod = {RequestMethod.GET, RequestMethod.POST};//todo 要从注解中来
        mh.setRequestMethods(reqMethod);
        return mh;
    }

    public static void main(String[] args) throws Exception {
        String compmentScan = "httpserver.controller";
        List<Class<?>> list = ClassPathCandidateComponentScanner.getClasssFromPackage(compmentScan);
        List<Class<?>> handles = new ArrayList<>();
        for (Class<?> aClass : list) {
            RequestMapping ano = aClass.getAnnotation(RequestMapping.class);
            if (ano != null) {
                handles.add(aClass);
            }
            System.out.println(aClass.getName());
        }

        //根据路由找到对应controller
        HashMap<String, Object> methodMap = new HashMap<>();
        for (Class<?> handle : handles) {
//todo 遍历所有的method
            //todo 以Methtod为key组件pathmap
            RequestMapping controllerRequestMap = handle.getAnnotation(RequestMapping.class);
            String controllerMapUrl = controllerRequestMap.value().length > 0 ? controllerRequestMap.value()[0] : "";
            for (Method method : handle.getMethods()) {
                RequestMapping requestMap = method.getAnnotation(RequestMapping.class);
                if (requestMap == null) {
                    continue;
                }
                String methodMapUrl = requestMap.value().length > 0 ? requestMap.value()[0] : "";//最后一个url
                String key = controllerMapUrl + "/" + methodMapUrl;
                methodMap.put(key, method);//形成url+method的map
                //还要把controller实例放进去
            }
        }

        String path = "/test/t1";
        Method handle = (Method) methodMap.get(path);
        RequestImpl<TestEntry> request = new RequestImpl<TestEntry>();
        TestEntry entry = new TestEntry("a", "ss");
        TestController tmp = new TestController();//一定要有实例化的controller todo 需要将controller都形成一个实例后保存在bean当中
        handle.invoke(tmp, request);
//Object result =
        //System.out.println(result);

    }

    /*
    public static RequestHandler init() throws Exception {
        //ApplicationContext applicationContext
        logger.info("init request mapping");

        Map<String, Object> handlerMap = applicationContext.getBeansWithAnnotation(RequestMapping.class);
        for (Map.Entry<String, Object> entry : handlerMap.entrySet()) {
            Class<?> handlerClass = entry.getValue().getClass();
            RequestMapping requestMapping = handlerClass.getAnnotation(RequestMapping.class);
            String[] baseUrl = requestMapping.value();
            if (baseUrl == null || baseUrl.length == 0) {
                baseUrl = new String[1];
                baseUrl[0] = "/" + entry.getKey();
            }
            RequestMethod[] defaultMethod = requestMapping.method();
            if (defaultMethod == null || defaultMethod.length == 0) {
                defaultMethod = new RequestMethod[1];
                defaultMethod[0] = RequestMethod.GET;
            }

            ResponseDataType responseDataType = handlerClass.getAnnotation(ResponseDataType.class);
            ExecutorGroup executorGroup = handlerClass.getAnnotation(ExecutorGroup.class);

            Object obj = entry.getValue();
            initHandlerByClass(pathMap, obj, handlerClass, baseUrl, defaultMethod, responseDataType, executorGroup);
        }
        applicationContext = applicationContext.getParent();
    }
        return pathMap;
}

    private static void initHandlerByClass(PathMap<RequestHandler> pathMap, Object obj, Class<?> handlerClass, String[] baseUrl, RequestMethod[] defaultMethod, ResponseDataType defaultResponseDataType, ExecutorGroup defaultExecutorGroup) throws Exception {
        Method[] methods = handlerClass.getDeclaredMethods();
        for (Method method : methods) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping == null)
                continue;
            ResponseDataType responseDataType = method.getAnnotation(ResponseDataType.class);
            IgnorePermission ignorePermission = method.getAnnotation(IgnorePermission.class);
            ExecutorGroup executorGroup = method.getAnnotation(ExecutorGroup.class);
            ClearPushBadge clearPushBadge = method.getAnnotation(ClearPushBadge.class);
            if (executorGroup == null) executorGroup = defaultExecutorGroup;

            String[] url = requestMapping.value();
            if (url == null || url.length == 0) {
                url = new String[baseUrl.length];
                for (int i = 0; i < baseUrl.length; i++) {
                    url[i] = URIUtil.addPaths(baseUrl[i], method.getName());
                }
            } else {
                String[] subUrl = url;
                url = new String[baseUrl.length * subUrl.length];
                for (int i = 0; i < baseUrl.length; i++) {
                    for (int j = 0; j < subUrl.length; j++) {
                        url[i * baseUrl.length + j] = URIUtil.addPaths(baseUrl[i], subUrl[j]);
                    }
                }
            }
            RequestMethod[] reqMethod = requestMapping.method();
            if (reqMethod == null || reqMethod.length == 0) {
                reqMethod = new RequestMethod[defaultMethod.length];
                System.arraycopy(defaultMethod, 0, reqMethod, 0, defaultMethod.length);
            }
            boolean permissionCheck = (ignorePermission == null) || !ignorePermission.value();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> dataTypeClz = getRequestDataType(method);
            initHandlerByMethod(pathMap, obj, handlerClass, method, parameterTypes, dataTypeClz, url, reqMethod,
                    (responseDataType == null ? defaultResponseDataType : responseDataType), permissionCheck, executorGroup, clearPushBadge);
        }
    }



    private static void initHandlerByMethod(PathMap<RequestHandler> pathMap,
                                            Object obj, Class<?> handlerClass, Method method,
                                            Class<?>[] parameterTypes, Class<?> dataType,
                                            String[] url, RequestMethod[] reqMethod,
                                            ResponseDataType responseDataType,
                                            boolean permissionCheck,
                                            ExecutorGroup executorGroup,
                                            ClearPushBadge clearPushBadge) throws Exception {
        int parameterOrder = RequestHandler.genParameterOrder(parameterTypes);
        if (parameterOrder < 0)
            return;
        for (String s : url) {
            RequestHandler mh = new RequestHandler();
            mh.setHandlerObj(obj);
            mh.setHandlerClass(handlerClass);
            mh.setMethod(method);
            mh.setUrl(s);
            mh.setParameter(parameterTypes, parameterOrder);
            mh.setDataType(dataType);
            mh.setRequestMethods(reqMethod);
            if (executorGroup != null && !StringUtils.isEmpty(executorGroup.name())) {
                mh.setExecutorGroupName(executorGroup.name().toUpperCase());
            }
            if (responseDataType != null) {
                mh.setResponseContentType(responseDataType.value());
                mh.setUseGzip(responseDataType.gzip());
            }
            mh.setPermisionCheck(permissionCheck);
            if (clearPushBadge != null) {
                mh.setClearPushBadge(clearPushBadge.value());
            }
            RequestHandler oldMethodHandler = pathMap.put(s, mh);
            if (!StringUtils.startsWith(s, "/manage/")) { // logger
                StringBuilder str = new StringBuilder("(");
                for (Class<?> clz : parameterTypes) {
                    if (str.length() > 1)
                        str.append(",");
                    str.append(clz.getSimpleName());
                    if (Request.class.equals(clz)) {
                        str.append("<").append(dataType.getSimpleName()).append(">");
                    }
                }
                str.append(")");
                String clzName = handlerClass.getName();
                if (clzName.startsWith("com.kingdee.mobile.eclite.handler.action."))
                    clzName = clzName.substring(41);
                if (oldMethodHandler != null) {
                    logger.info("replace mapping {} to {}.{}{}", s, clzName, method.getName(), str);
                } else {
                    logger.info("  add   mapping {} to {}.{}{}", s, clzName, method.getName(), str);
                }
                ActionRequestCounter.getInstance().init(mh.toSimpleString());
            }
        }
    }
    */
    private static Class getRequestDataType(Method method) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?> dataTypeClz = StringMap.class;
        for (Type genericParameterType : genericParameterTypes) {
            if (genericParameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                if (Request.class.equals(parameterizedType.getRawType())) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                        Type actualTypeArgument = actualTypeArguments[0];
                        if (actualTypeArgument instanceof ParameterizedType) {
                            dataTypeClz = (Class) ((ParameterizedType) actualTypeArgument).getRawType();
                        } else if (actualTypeArgument instanceof Class) {
                            dataTypeClz = (Class) actualTypeArgument;
                        }
                    }
                    break;
                }
            } else if (Request.class.equals(genericParameterType)) {
                dataTypeClz = StringMap.class;
                break;
            }
        }
        if (Object.class.equals(dataTypeClz)) {
            dataTypeClz = StringMap.class;
        }
        return dataTypeClz;
    }

}