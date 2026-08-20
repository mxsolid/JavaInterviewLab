package com.javainterviewlab.common.logging;

import org.slf4j.MDC;

/**
 * 请求级 traceId 访问入口，避免业务代码直接依赖 MDC 的固定键名。
 */
public final class TraceIdContext {

    public static final String MDC_KEY = "traceId";

    private TraceIdContext() {
    }

    public static String getTraceId() {
        return MDC.get(MDC_KEY);
    }

    static void put(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }

    static void clear() {
        MDC.remove(MDC_KEY);
    }
}
