package cn.czu.claimpaws.common.api;

/** Standard envelope returned by ClaimPaws HTTP APIs. */
public record ApiResponse<T>(boolean success, T data, String code, String message, String requestId) {

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(true, data, null, null, requestId);
    }

    public static <T> ApiResponse<T> failure(String code, String message, String requestId) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("error code is required");
        }
        return new ApiResponse<>(false, null, code, message, requestId);
    }
}
