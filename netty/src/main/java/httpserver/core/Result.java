package httpserver.core;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * User: Janon
 * Date: 13-11-10 上午11:44
 */
public class Result {
    public static final Result EMPTY = Result.build((Object) null);

    public static final int COMMON_ERROR = ErrorEnum.ERROR.getCode();
    private boolean success;
    private String error;
    private Object data;
    private int errorCode = 0;

    @JSONField(serialize = false, deserialize = false)
    private boolean needConvert = false;
    @JSONField(serialize = false, deserialize = false)
    private Object[] params = null;

    public static Result build(Object data) {
        Result result = new Result();
        result.success = true;
        result.data = data;
        return result;
    }

    public static Result build(ErrorEnum e) {
        return build(e, (Object) null);
    }

    public static Result build(ErrorEnum e, Object... params) {
        return build(null, e, params);
    }

    public static Result build(Object data, ErrorEnum error, Object... params) {
        Result result = new Result();
        result.data = data;
        result.error = error.getError();
        result.errorCode = error.getCode();
        result.params = params;
        result.needConvert = true;
        return result;
    }


    public static Result build(int errorCode, String error) {
        return build(errorCode, error, null);
    }

    public static Result build(int errorCode, String error, Object data) {
        Result result = new Result();
        result.success = false;
        result.errorCode = errorCode;
        result.error = error;
        result.data = data;
        return result;
    }

    public static Result build(Throwable e) {
        Result result ;
        if (e instanceof NullPointerException) {
            result = build(ErrorEnum.ERROR_NULL_POINT);
        }else {
            result = new Result();
            result.success = false;
            result.errorCode = COMMON_ERROR;
            result.error = e.getMessage();
        }
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public Object getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void convertError(String error){
        this.error = error;
    }

    @JSONField(serialize = false, deserialize = false)
    public Object[] getParams() {
        return params;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isNeedConvert() {
        return needConvert;
    }
}
