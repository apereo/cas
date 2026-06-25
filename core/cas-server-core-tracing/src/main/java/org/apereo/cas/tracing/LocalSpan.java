package org.apereo.cas.tracing;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link org.apereo.cas.tracing.LocalSpan}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record LocalSpan(
    String traceId,
    String spanId,
    @Nullable String parentSpanId,
    String name,
    String serviceName,
    String kind,
    long startEpochNanos,
    long durationNanos,
    Map<String, String> attributes,
    boolean error
) {}
