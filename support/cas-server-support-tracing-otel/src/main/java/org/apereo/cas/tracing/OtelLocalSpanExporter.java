package org.apereo.cas.tracing;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ServiceAttributes;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OtelLocalSpanExporter}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OtelLocalSpanExporter implements SpanExporter {
    private static final AttributeKey<String> URL_PATH =
        AttributeKey.stringKey("url.path");

    private static final AttributeKey<String> HTTP_ROUTE =
        AttributeKey.stringKey("http.route");

    private static final AttributeKey<String> HTTP_TARGET =
        AttributeKey.stringKey("http.target");

    private static final AttributeKey<String> HTTP_URL =
        AttributeKey.stringKey("http.url");

    private final LocalTraceStore store;

    @Override
    public CompletableResultCode export(final Collection<SpanData> spans) {
        for (val span : spans) {
            if (!shouldIgnore(span)) {
                store.add(toLocalSpan(span));
            }
        }
        return CompletableResultCode.ofSuccess();
    }


    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    private static LocalSpan toLocalSpan(final SpanData span) {
        val attributes = new HashMap<String, String>();
        span.getAttributes().forEach((key, value) -> attributes.put(key.getKey(), String.valueOf(value)));

        var serviceName = span.getResource().getAttribute(ServiceAttributes.SERVICE_NAME);
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = "cas";
        }

        val error = span.getStatus().getStatusCode() == StatusData.error().getStatusCode();
        val kind = span.getKind() != null ? span.getKind().name() : "INTERNAL";

        return new LocalSpan(
            span.getTraceId(),
            span.getSpanId(),
            span.getParentSpanId(),
            span.getName(),
            serviceName,
            kind,
            span.getStartEpochNanos(),
            span.getEndEpochNanos() - span.getStartEpochNanos(),
            attributes,
            error
        );
    }

    private static boolean shouldIgnore(final SpanData span) {
        val spanName = span.getName();
        return shouldIgnore(span.getAttributes().get(URL_PATH))
            || shouldIgnore(span.getAttributes().get(HTTP_ROUTE))
            || shouldIgnore(span.getAttributes().get(HTTP_TARGET))
            || shouldIgnore(span.getAttributes().get(HTTP_URL))
            || shouldIgnore(spanName);

    }

    private static boolean shouldIgnore(final @Nullable String value) {
        return RegexUtils.find(".*(/webjars/|/css/|/js/|/images/|favicon|/actuator/|/palantir/).*", value);
    }
}
