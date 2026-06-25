package org.apereo.cas.tracing;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This is {@link org.apereo.cas.tracing.LocalTraceDetail}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record LocalTraceDetail(
    String traceId,
    List<LocalSpan> spans,
    long durationNanos
) {
}
