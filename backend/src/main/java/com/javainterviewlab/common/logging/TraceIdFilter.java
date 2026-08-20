package com.javainterviewlab.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 为每个 HTTP 请求建立日志关联标识。
 *
 * <p>优先透传合法的上游标识，使网关和后端日志能关联；非法标识改为服务端生成，避免把任意请求头写入日志和响应头。</p>
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final int TRACE_ID_LENGTH = 6;
    private static final String TRACE_ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Z0-9]{" + TRACE_ID_LENGTH + "}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        TraceIdContext.put(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 线程会被 Servlet 容器复用，必须清理，避免下一请求串用旧标识。
            TraceIdContext.clear();
        }
    }

    private String resolveTraceId(String requestedTraceId) {
        if (StringUtils.hasText(requestedTraceId) && TRACE_ID_PATTERN.matcher(requestedTraceId).matches()) {
            return requestedTraceId;
        }
        StringBuilder traceId = new StringBuilder(TRACE_ID_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < TRACE_ID_LENGTH; index++) {
            traceId.append(TRACE_ID_ALPHABET.charAt(random.nextInt(TRACE_ID_ALPHABET.length())));
        }
        return traceId.toString();
    }
}
