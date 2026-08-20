package com.javainterviewlab.common.exception;

import com.javainterviewlab.common.api.ApiErrorCode;

/**
 * 业务规则未满足时抛出的受控异常。
 *
 * <p>异常只携带已定义的错误码和对调用方安全的消息，避免把底层实现细节暴露到 API。</p>
 */
public class BusinessException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public BusinessException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
