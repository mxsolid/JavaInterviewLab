package com.javainterviewlab.common.exception;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.common.logging.TraceIdContext;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将框架异常和业务异常统一转换为 API 响应，避免每个控制器重复处理错误分支。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return response(exception.getErrorCode(), exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return response(ApiErrorCode.VALIDATION_FAILED, ApiErrorCode.VALIDATION_FAILED.getDefaultMessage(), fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return response(ApiErrorCode.VALIDATION_FAILED, exception.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        return response(ApiErrorCode.REQUEST_BODY_INVALID, ApiErrorCode.REQUEST_BODY_INVALID.getDefaultMessage(), null);
    }

    /** 上传层先于 Controller 解析 multipart，超限也必须返回稳定的内容校验错误。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception
    ) {
        return response(ApiErrorCode.CONTENT_VALIDATION_FAILED, "种子文件不能超过 10MB", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        LOGGER.warn("数据约束未满足，traceId={}", TraceIdContext.getTraceId());
        return response(ApiErrorCode.BUSINESS_RULE_VIOLATED, "数据已存在或仍被关联，无法完成操作", null);
    }

    /**
     * 浏览器会自动请求 favicon.ico；未提供静态资源属于正常 404，不应进入未处理异常日志。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException exception) {
        return response(ApiErrorCode.RESOURCE_NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("未处理异常，traceId={}", TraceIdContext.getTraceId(), exception);
        return response(ApiErrorCode.INTERNAL_ERROR, ApiErrorCode.INTERNAL_ERROR.getDefaultMessage(), null);
    }

    private <T> ResponseEntity<ApiResponse<T>> response(ApiErrorCode errorCode, String message, T data) {
        HttpStatus httpStatus = errorCode.getHttpStatus();
        return ResponseEntity.status(httpStatus).body(ApiResponse.failure(errorCode, message, data));
    }
}
