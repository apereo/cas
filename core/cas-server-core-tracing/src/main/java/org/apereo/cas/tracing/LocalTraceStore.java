package org.apereo.cas.tracing;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link LocalTraceStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class LocalTraceStore {
    /**
     * Default bean name for the local trace store.
     */
    public static final String BEAN_NAME = "localTraceStore";

    private static final int MAX_TRACES = 1_000;

    private final Map<String, List<LocalSpan>> spansByTraceId = new ConcurrentHashMap<>();
    private final Deque<String> traceOrder = new ArrayDeque<>();

    /**
     * Add.
     *
     * @param span the span
     */
    public void add(final LocalSpan span) {
        val newTrace = !spansByTraceId.containsKey(span.traceId());

        spansByTraceId
            .computeIfAbsent(span.traceId(), id -> new ArrayList<>())
            .add(span);

        if (newTrace) {
            traceOrder.addFirst(span.traceId());
        }

        while (traceOrder.size() > MAX_TRACES) {
            val removedTraceId = traceOrder.removeLast();
            spansByTraceId.remove(removedTraceId);
        }
    }

    /**
     * Summaries list.
     *
     * @return the list
     */
    public List<LocalTraceSummary> summaries() {
        return traceOrder
            .stream()
            .map(this::summary)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Find by trace id.
     *
     * @param traceId the trace id
     * @return the details
     */
    public Optional<LocalTraceDetail> find(final String traceId) {
        val spans = spansByTraceId.get(traceId);
        if (spans == null || spans.isEmpty()) {
            return Optional.empty();
        }

        val sorted = spans.stream()
            .sorted(Comparator.comparingLong(LocalSpan::startEpochNanos))
            .toList();

        val start = sorted.stream().mapToLong(LocalSpan::startEpochNanos).min().orElse(0);
        val end = sorted.stream()
            .mapToLong(s -> s.startEpochNanos() + s.durationNanos())
            .max()
            .orElse(start);

        return Optional.of(new LocalTraceDetail(traceId, sorted, end - start));
    }

    private @Nullable LocalTraceSummary summary(final String traceId) {
        val spans = spansByTraceId.get(traceId);
        if (spans == null || spans.isEmpty()) {
            return null;
        }

        val sorted = spans.stream()
            .sorted(Comparator.comparingLong(LocalSpan::startEpochNanos))
            .toList();

        val root = sorted.stream()
            .filter(span -> "SERVER".equalsIgnoreCase(span.kind()))
            .findFirst()
            .orElseGet(() -> sorted.stream()
                .filter(span -> span.parentSpanId() == null || span.parentSpanId().isBlank())
                .findFirst()
                .orElseGet(sorted::getFirst));

        val method = firstNonBlank(
            attr(root, "http.request.method"),
            attr(root, "http.method"),
            attr(root, "method"),
            methodFromSpanName(root.name())
        );

        if (StringUtils.isBlank(method)) {
            return null;
        }
        
        val start = sorted.stream()
            .mapToLong(LocalSpan::startEpochNanos)
            .min()
            .orElse(0);

        val end = sorted.stream()
            .mapToLong(span -> span.startEpochNanos() + span.durationNanos())
            .max()
            .orElse(start);

        val services = new TreeSet<String>();
        var error = false;

        for (val span : sorted) {
            services.add(span.serviceName());
            if (span.error()) {
                error = true;
            }
        }

        val route = firstNonBlank(
            attr(root, "http.route")
        );

        val url = firstNonBlank(
            pathWithQuery(root),
            attr(root, "http.target"),
            attr(root, "url.path"),
            attr(root, "http.url")
        );

        return new LocalTraceSummary(
            traceId,
            Instant.ofEpochSecond(0, start),
            root.name(),
            method,
            url,
            route,
            services,
            sorted.size(),
            end - start,
            error
        );
    }

    private static @Nullable String attr(final LocalSpan span, final String name) {
        return span.attributes().get(name);
    }

    private static @Nullable String pathWithQuery(final LocalSpan span) {
        val path = attr(span, "url.path");
        val query = attr(span, "url.query");

        if (path == null || path.isBlank()) {
            return null;
        }

        if (query == null || query.isBlank()) {
            return path;
        }

        return path + '?' + query;
    }

    private static @Nullable String firstNonBlank(final String... values) {
        return Arrays.stream(values).filter(value -> value != null && !value.isBlank()).findFirst().orElse(null);
    }

    private static @Nullable String methodFromSpanName(final String spanName) {
        if (spanName == null || spanName.isBlank()) {
            return null;
        }
        val normalized = spanName.trim().toUpperCase(Locale.ROOT);
        if ("HTTP GET".equals(normalized) || normalized.endsWith(" GET")) {
            return "GET";
        }
        if ("HTTP POST".equals(normalized) || normalized.endsWith(" POST")) {
            return "POST";
        }
        if ("HTTP PUT".equals(normalized) || normalized.endsWith(" PUT")) {
            return "PUT";
        }
        if ("HTTP PATCH".equals(normalized) || normalized.endsWith(" PATCH")) {
            return "PATCH";
        }
        if ("HTTP DELETE".equals(normalized) || normalized.endsWith(" DELETE")) {
            return "DELETE";
        }
        if ("HTTP OPTIONS".equals(normalized) || normalized.endsWith(" OPTIONS")) {
            return "OPTIONS";
        }
        if ("HTTP HEAD".equals(normalized) || normalized.endsWith(" HEAD")) {
            return "HEAD";
        }

        return null;
    }
}
