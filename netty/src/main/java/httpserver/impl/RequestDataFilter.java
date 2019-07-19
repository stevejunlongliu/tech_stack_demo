package httpserver.impl;

import httpserver.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RequestDataFilter
 * Created by Janon on 2015/9/15.
 */
public class RequestDataFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestDataFilter.class);
    private static RequestDataFilter instance = new RequestDataFilter();

    public static RequestDataFilter getInstance() {
        if (instance == null) {
            instance = new RequestDataFilter();
        }
        return instance;
    }

    private Lock lock = new ReentrantLock();
    private Map<String, DataClass> cache = new HashMap<String, DataClass>();

    private RequestDataFilter() {
    }

    private class DataClass {
        private Method getCurrent3gNo;
        private Method setCurrent3gNo;

        private Method getCurrentUserId;
        private Method setCurrentUserId;

        private Method getGroupId;
        private Method setGroupId;

        private Method getUserId;
        private Method setUserId;

        private Method getToUserId;
        private Method setToUserId;

        public DataClass(Class<?> clazz) {
            Field[] declaredFields = DataClass.class.getDeclaredFields();
            Map<String, Field> fieldMapping = new HashMap<String, Field>();
            for (Field field : declaredFields) {
                fieldMapping.put(field.getName(), field);
            }
            for (Map.Entry<String, Field> entry : fieldMapping.entrySet()) {
                String name = entry.getKey();
                Field field = entry.getValue();
                if (StringUtils.startsWith(name, "set")) {
                    try {
                        Method method = clazz.getMethod(name, String.class);
                        field.set(this, method);
                    } catch (Throwable ignore) {
                    }
                } else if (StringUtils.startsWith(name, "get")) {
                    try {
                        Method method = clazz.getMethod(name);
                        if (method.getReturnType() == String.class) {
                            field.set(this, method);
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }
        }

        public boolean hasMethodRequester() {
            return getCurrent3gNo != null && setCurrent3gNo != null && getCurrentUserId != null && setCurrentUserId != null;
        }

        public boolean hasMethodGroupId() {
            return getGroupId != null && setGroupId != null;
        }

        public boolean hasMethodUserId() {
            return getUserId != null && setUserId != null;
        }

        public boolean hasMethodToUserId() {
            return getToUserId != null && setToUserId != null;
        }

        public String getCurrent3gNo(Object data) {
            if (getCurrent3gNo == null) return null;
            try {
                return (String) getCurrent3gNo.invoke(data);
            } catch (Throwable ignore) {
                logger.warn(ignore.getMessage(), ignore);
            }
            return null;
        }

        public String getCurrentUserId(Object data) {
            if (getCurrentUserId == null) return null;
            try {
                return (String) getCurrentUserId.invoke(data);
            } catch (Throwable ignore) {
                logger.warn(ignore.getMessage(), ignore);
            }
            return null;
        }

        public void setCurrentUser(Object data, String current3gNo, String userId) {
            if (setCurrentUserId != null) {
                try {
                    setCurrentUserId.invoke(data, userId);
                } catch (Throwable ignore) {
                    logger.warn(ignore.getMessage(), ignore);
                }
            }
            if (setCurrent3gNo != null) {
                try {
                    setCurrent3gNo.invoke(data, current3gNo);
                } catch (Throwable ignore) {
                    logger.warn(ignore.getMessage(), ignore);
                }
            }
        }

        public String getGroupId(Object data) {
            if (getGroupId == null) return null;
            try {
                return (String) getGroupId.invoke(data);
            } catch (Throwable ignore) {
                logger.warn(ignore.getMessage(), ignore);
            }
            return null;
        }

        public void setGroupId(Object data, String groupId) {
            if (setGroupId != null) {
                try {
                    setGroupId.invoke(data, groupId);
                } catch (Throwable ignore) {
                    logger.warn(ignore.getMessage(), ignore);
                }
            }
        }

        public String getUserId(Object data) {
            if (getUserId == null) return null;
            try {
                Object rtn = getUserId.invoke(data);
                if (rtn instanceof String) {
                    return (String) rtn;
                }
            } catch (Throwable ignore) {
                logger.warn(ignore.getMessage(), ignore);
            }
            return null;
        }

        public void setUserId(Object data, String userId) {
            if (setUserId != null) {
                try {
                    setUserId.invoke(data, userId);
                } catch (Throwable ignore) {
                    logger.warn(ignore.getMessage(), ignore);
                }
            }
        }

        public String getToUserId(Object data) {
            if (getToUserId == null) return null;
            try {
                return (String) getToUserId.invoke(data);
            } catch (Throwable ignore) {
                logger.warn(ignore.getMessage(), ignore);
            }
            return null;
        }

        public void setToUserId(Object data, String userId) {
            if (setToUserId != null) {
                try {
                    setToUserId.invoke(data, userId);
                } catch (Throwable ignore) {
                    logger.warn(ignore.getMessage(), ignore);
                }
            }
        }
    }

    public void filter(Object data) {
        if (data == null) return;
        if (data instanceof StringMap) return;
        Class<?> dataClass = data.getClass();
        String dataClassName = dataClass.getName();
        DataClass dataClassWrap = cache.get(dataClassName);
        if (dataClassWrap == null) {
            lock.lock();
            try {
                dataClassWrap = cache.get(dataClassName);
                if (dataClassWrap == null) {
                    dataClassWrap = new DataClass(dataClass);
                    cache.put(dataClassName, dataClassWrap);
                }
            } finally {
                lock.unlock();
            }
        }

        if (!dataClassWrap.hasMethodRequester()) return;
        String current3gNo = dataClassWrap.getCurrent3gNo(data);
        String currentUserId = dataClassWrap.getCurrentUserId(data);
        if (StringUtils.isEmpty(current3gNo) || StringUtils.isEmpty(currentUserId)) return;
        if (StringUtils.startsWith(currentUserId, "manager/")) return;

//        if (dataClassWrap.hasMethodGroupId()) {
//            String groupId = dataClassWrap.getGroupId(data);
//            if (StringUtils.endsWith(groupId, "_ext")) {
//                setCurrentUserToExtPerson(data, dataClassName, dataClassWrap, currentUserId);
//                return;
//            }
//        }
//        if (dataClassWrap.hasMethodUserId()) {
//            String userId = dataClassWrap.getUserId(data);
//            if (EcLiteUtil.isExtId(userId)) {
//                setCurrentUserToExtPerson(data, dataClassName, dataClassWrap, currentUserId);
//                return;
//            }
//        }
//        if (dataClassWrap.hasMethodToUserId()) {
//            String userId = dataClassWrap.getToUserId(data);
//            if (EcLiteUtil.isExtId(userId)) {
//                setCurrentUserToExtPerson(data, dataClassName, dataClassWrap, currentUserId);
//            }
//        }
    }


}
