package org.apereo.cas.logging;

import module java.base;
import com.google.cloud.spring.logging.StackdriverTraceConstants;
import com.google.cloud.spring.logging.extractors.CloudTraceIdExtractor;
import com.google.cloud.spring.logging.extractors.TraceIdExtractor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link GoogleCloudLoggingWebInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Getter
public class GoogleCloudLoggingWebInterceptor implements HandlerInterceptor {
    /**
     * B3 Propagation is a specification for the header "b3" and those that start with "x-b3-". These
     * headers are used for trace context propagation across service boundaries.
     * Trace identifiers are 64 or 128-bit, but all span identifiers within a trace are 64-bit.
     * All identifiers are opaque.
     */
    public static final String HEADER_B3_TRACE_ID = "X-B3-TraceId";

    private final TraceIdExtractor traceIdExtractor = new CloudTraceIdExtractor();

    @Override
    public boolean preHandle(final @NonNull HttpServletRequest httpServletRequest,
                             final @NonNull HttpServletResponse httpServletResponse,
                             final @NonNull Object handler) {
        val traceId = StringUtils.defaultIfBlank(
            traceIdExtractor.extractTraceIdFromRequest(httpServletRequest),
            httpServletRequest.getHeader(HEADER_B3_TRACE_ID));
        ThreadContext.put(StackdriverTraceConstants.MDC_FIELD_TRACE_ID, traceId);
        var url = httpServletRequest.getRequestURL().toString();
        val queryString = httpServletRequest.getQueryString();
        if (queryString != null) {
            url += '?' + queryString;
        }
        ThreadContext.put("requestUrl", url);
        return true;
    }
}
