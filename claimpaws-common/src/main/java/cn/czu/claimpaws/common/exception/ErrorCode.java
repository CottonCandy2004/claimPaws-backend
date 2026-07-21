package cn.czu.claimpaws.common.exception;

public enum ErrorCode {
    RESERVATION_TIME_CONFLICT("4001", "预约时间冲突"),
    RESOURCE_NOT_FOUND("4002", "资源不存在"),
    RESOURCE_SERVICE_UNAVAILABLE("4003", "资源服务不可用"),
    VALIDATION_FAILED("4004", "参数校验失败"),
    IDEMPOTENCY_KEY_REUSED("4005", "幂等键重复");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
