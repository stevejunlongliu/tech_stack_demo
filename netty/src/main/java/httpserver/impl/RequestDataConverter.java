package httpserver.impl;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class RequestDataConverter {
    private static RequestDataConverter instance = new RequestDataConverter();

    public static RequestDataConverter getInstance() {
        if (instance == null) {
            instance = new RequestDataConverter();
        }
        return instance;
    }

    private RequestDataConverter() {
    }

    private Map<String, List<String>> cache = new HashMap<String, List<String>>();
    private Lock lock = new ReentrantLock();

    public <T> T convert(Map<String, Object> data, Class<T> clazz) throws Exception {
        String clazzName = clazz.getName();
        List<String> list = cache.get(clazzName);
        if (list == null) {
            Field[] fields = clazz.getDeclaredFields();
            list = new ArrayList<String>(2);
            for (Field field : fields) {
                Class<?> fieldType = field.getType();
                if (String[].class == fieldType
                        || long[].class == fieldType
                        || int[].class == fieldType
                        || List.class.isAssignableFrom(fieldType)) {
                    list.add(field.getName());
                }
            }
            lock.lock();
            try {
                if (!cache.containsKey(clazzName)) {
                    if (list.isEmpty()) list = Collections.emptyList();
                    cache.put(clazzName, list);
                }
            } finally {
                lock.unlock();
            }
        }
        if (!list.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.putAll(data);
            for (String name : list) {
                Object o = params.get(name);
                if (o == null) continue;
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                    params.put(name, o);
                }
            }
            data = params;
        }
        return TypeUtils.cast(data, clazz, ParserConfig.getGlobalInstance());
    }
}
