package org.apereo.cas.tracing;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link org.apereo.cas.tracing.LocalTraceSummary}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record LocalTraceSummary(
    String traceId,
    Instant startedAt,
    String rootSpan,
    @Nullable String method,
    @Nullable String url,
    @Nullable String route,
    Set<String> services,
    int spanCount,
    long durationNanos,
    boolean error
) {}
