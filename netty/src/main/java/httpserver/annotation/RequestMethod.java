package httpserver.annotation;

import org.apache.commons.codec.binary.StringUtils;

public enum RequestMethod {
    GET,
    POST;//暂时只支持这两种

    private RequestMethod() {
    }

    public static RequestMethod getEnum(String value) {

        RequestMethod rtn = null;

        for (RequestMethod method : RequestMethod.values()) {
            if (StringUtils.equals(method.name(), value)) {
                rtn = method;
                break;
            }
        }

        return rtn;
    }
}
