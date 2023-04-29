package org.apereo.cas.logging;

import com.google.cloud.spring.logging.StackdriverTraceConstants;
import com.google.cloud.spring.logging.extractors.CloudTraceIdExtractor;
import com.google.cloud.spring.logging.extractors.TraceIdExtractor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * This is {@link GoogleCloudLoggingWebInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GoogleCloudLoggingWebInterceptor implements WebRequestInterceptor {

    /**
     * MDC field for HTTP request method.
     */
    public static final String MDC_FIELD_HTTP_REQUEST_METHOD = "http.requestMethod";

    /**
     * MDC field for HTTP request URL.
     */
    public static final String MDC_FIELD_HTTP_REQUEST_URL = "http.requestUrl";

    /**
     * MDC field for HTTP response status.
     */
    public static final String MDC_FIELD_HTTP_STATUS = "http.status";

    /**
     * MDC field for HTTP response size.
     */
    public static final String MDC_FIELD_HTTP_RESPONSE_SIZE = "http.responseSize";

    /**
     * MDC field for HTTP request user agent.
     */
    public static final String MDC_FIELD_HTTP_USER_AGENT = "http.userAgent";

    /**
     * MDC field for HTTP request remote ip.
     */
    public static final String MDC_FIELD_HTTP_REMOTE_IP = "http.remoteIp";

    /**
     * MDC field for HTTP protocol.
     */
    public static final String MDC_FIELD_HTTP_PROTOCOL = "http.protocol";

    public final TraceIdExtractor traceIdExtractor = new CloudTraceIdExtractor();

    @Override
    public void preHandle(final WebRequest webRequest) {
        val httpServletRequest = ((ServletRequestAttributes) webRequest).getRequest();
        var traceId = traceIdExtractor.extractTraceIdFromRequest(httpServletRequest);
        if (StringUtils.isBlank(traceId)) {
            traceId = webRequest.getHeader("X-B3-TraceId");
        }
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put(StackdriverTraceConstants.MDC_FIELD_TRACE_ID, traceId);
        }
        val httpServletResponse = ((ServletRequestAttributes) webRequest).getResponse();
        MDC.put(MDC_FIELD_HTTP_REQUEST_METHOD, httpServletRequest.getMethod());
        MDC.put(MDC_FIELD_HTTP_REQUEST_URL, httpServletRequest.getRequestURL().toString());
        MDC.put(MDC_FIELD_HTTP_STATUS, String.valueOf(httpServletResponse.getStatus()));
        MDC.put(MDC_FIELD_HTTP_RESPONSE_SIZE, String.valueOf(httpServletResponse.getBufferSize()));
        MDC.put(MDC_FIELD_HTTP_USER_AGENT, httpServletRequest.getHeader("User-Agent"));
        MDC.put(MDC_FIELD_HTTP_REMOTE_IP, httpServletRequest.getRemoteAddr());
        MDC.put(MDC_FIELD_HTTP_PROTOCOL, httpServletRequest.getProtocol());
    }

    @Override
    public void postHandle(final WebRequest request, final ModelMap model) {
    }

    @Override
    public void afterCompletion(final WebRequest request, final Exception ex) {
        MDC.remove(StackdriverTraceConstants.MDC_FIELD_TRACE_ID);
        MDC.remove(MDC_FIELD_HTTP_REQUEST_METHOD);
        MDC.remove(MDC_FIELD_HTTP_REQUEST_URL);
        MDC.remove(MDC_FIELD_HTTP_STATUS);
        MDC.remove(MDC_FIELD_HTTP_RESPONSE_SIZE);
        MDC.remove(MDC_FIELD_HTTP_USER_AGENT);
        MDC.remove(MDC_FIELD_HTTP_REMOTE_IP);
        MDC.remove(MDC_FIELD_HTTP_PROTOCOL);
    }
}
