package com.javainterviewlab.common.api;

import com.javainterviewlab.common.logging.TraceIdContext;

/**
 * 所有业务 API 共用的响应结构。
 *
 * <p>traceId 在响应创建时从当前请求上下文读取，使前端报错信息能与服务端日志关联。</p>
 *
 * @param success 是否成功
 * @param code 业务响应码
 * @param message 面向调用方的简短说明
 * @param data 响应数据
 * @param traceId 请求链路标识
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        String traceId
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                ApiErrorCode.OK.getCode(),
                ApiErrorCode.OK.getDefaultMessage(),
                data,
                TraceIdContext.getTraceId()
        );
    }

    public static <T> ApiResponse<T> failure(ApiErrorCode errorCode, String message, T data) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                message,
                data,
                TraceIdContext.getTraceId()
        );
    }

    public static ApiResponse<Void> failure(ApiErrorCode errorCode) {
        return failure(errorCode, errorCode.getDefaultMessage(), null);
    }
}
