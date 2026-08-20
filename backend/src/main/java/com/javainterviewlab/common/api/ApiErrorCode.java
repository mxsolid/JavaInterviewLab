package com.javainterviewlab.common.api;

import org.springframework.http.HttpStatus;

/**
 * API 错误码集中定义，避免控制器和异常处理器散落字符串。
 */
public enum ApiErrorCode {

    OK("OK", "成功", HttpStatus.OK),
    VALIDATION_FAILED("VALIDATION_FAILED", "请求参数不合法", HttpStatus.BAD_REQUEST),
    REQUEST_BODY_INVALID("REQUEST_BODY_INVALID", "请求体格式不合法", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    VERSION_CONFLICT("VERSION_CONFLICT", "内容已被其他操作更新，请刷新后重试", HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATED("BUSINESS_RULE_VIOLATED", "当前操作不被允许", HttpStatus.CONFLICT),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统异常，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
