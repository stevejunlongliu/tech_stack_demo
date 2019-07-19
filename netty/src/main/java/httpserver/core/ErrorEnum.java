package httpserver.core;

/**
 * XT异常规则约定：xxyyzzz
 * xx  : 模块(10开始) 如群组、消息、用户、文件等
 * yy  : 子模块(01开始) 如群组消息、群组用户、消息未读等
 * zzz : 异常编号(001开始)
 *
 * 枚举命名规则：1个参数_1结尾，2个参数_2结尾，以此类推
 */
public enum ErrorEnum {
    SUCCESS(0, "message.common.success", false),
    ERROR(1, "message.common.error", false),
    //常用错误 10
    ERROR_PARAM                   (10_01_001, "error.common.param", false),
    ERROR_PARAM_1                 (10_01_002, "error.common.param.1", true),
    ERROR_PARAM_2                 (10_01_003, "error.common.param.2", true),
    ERROR_NULL_POINT              (10_01_004, "error.common.nullPoint", false),
    ERROR_ILLEGAL_REQUEST         (10_01_005, "error.common.illegalRequest", false),
    ERROR_IS_EMPTY_1              (10_01_006, "error.common.isEmpty", true),
    ERROR_PERMISSION_DENIED       (10_01_007, "error.common.permission.denied", false),
    ERROR_CLIENT_VERSION_TOO_LOW  (10_01_008, "error.common.client.version.too.low", false),
    ERROR_FORMAT_1                (10_01_009, "error.common.format", true),
    ERROR_DATE_FORMAT_1           (10_01_010, "error.common.date.format", true),
    ERROR_TOO_LARGE_1             (10_01_011, "error.common.too.large", true),
    ERROR_NOT_EXISTS_1            (10_01_012, "error.common.not.exists", true),
    ERROR_AUTH_TOKEN              (10_01_013, "error.common.auth.token", false),
    ERROR_HANDSHAKER              (10_01_014, "error.common.handshaker", false),
    ERROR_INVALID_1               (10_01_015, "error.common.invalid", true),
    ERROR_DELETE_ERROR            (10_01_016, "error.common.delete", false),
    ERROR_UNSUPPORTED             (10_01_017, "error.common.unsupported", false),
    ERROR_UNSUPPORTED_1           (10_01_018, "error.common.unsupported.1", true),
    ERROR_DECODE                  (10_01_019, "error.common.decode", false),
    ERROR_APP_CLIENT_ID_INVALID   (10_01_020, "error.common.app.clientId.invalid", false),
    ERROR_APP_CLIENT_ID_INCORRECT (10_01_021, "error.common.app.clientId.incorrect", false),
    ERROR_LOCK_FAILURE            (10_01_022, "error.common.lock.failure", false),
    //其他服务 11
    ERROR_SERVER_TODO            (11_01_001, "error.server.todo", false),
    ERROR_SERVER_SEARCH_DISABLE  (11_01_002, "error.server.search.disable", false),
    ERROR_SERVER_OSS             (11_01_003, "error.server.oss", false),
    ERROR_SERVER_OSS_DISABLE     (11_01_004, "error.server.oss.disable", false),
    ERROR_SERVER_DOCREST_DISABLE (11_01_005, "error.server.docrest.disable", false),
    ERROR_SERVER_VOICE           (11_01_006, "error.server.voice", false),
    ERROR_SERVER_SPACE           (11_01_007, "error.server.space", false),
    //用户12
    ERROR_USER_OPENTOKEN_EXPIRE  (12_01_001, "error.user.openToken.expire", false),
    ERROR_USER_NO_EXT_ID         (12_01_002, "error.user.no.extId", false),
    ERROR_USER_NO_OPEN_ID        (12_01_003, "error.user.no.openId", false),
    ERROR_USER_NOT_EXISTS        (12_01_004, "error.user.not.exists", false),
    ERROR_USER_NOT_EXISTS_1      (12_01_005, "error.user.not.exists.1", true),
    ERROR_USER_NOT_IN_GROUP_2    (12_01_006, "error.user.not.in.group", true),
    ERROR_USER_NO_NOT_EXISTS     (12_01_007, "error.user.not.in.group", false),
    ERROR_USER_DISABLED          (12_01_008, "error.user.disabled", false),

    ;

    private int code;
    private String error;
    private boolean needParam;

    ErrorEnum(int code, String resCode, boolean needParam) {
        this.code = code;
        this.error = resCode;
        this.needParam = needParam;
    }


    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public boolean isNeedParam() {
        return needParam;
    }
}
