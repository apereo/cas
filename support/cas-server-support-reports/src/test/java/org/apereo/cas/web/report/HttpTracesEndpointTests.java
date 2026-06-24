package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.config.CasCoreTracingAutoConfiguration;
import org.apereo.cas.tracing.LocalSpan;
import org.apereo.cas.tracing.LocalTraceStore;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link HttpTracesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = "management.endpoint.httptraces.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Execution(ExecutionMode.SAME_THREAD)
@ImportAutoConfiguration(CasCoreTracingAutoConfiguration.class)
class HttpTracesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(LocalTraceStore.BEAN_NAME)
    private LocalTraceStore localTraceStore;

    @Test
    void verifySummariesOperation() throws Exception {
        val traceId = UUID.randomUUID().toString();
        localTraceStore.add(span(traceId, "span-1", null, "HTTP POST", "cas", "SERVER", 100, 50,
            Map.of("url.path", "/cas/login", "url.query", "service=https://example.org",
                "http.route", "/login", "http.request.method", "POST"), false));

        mockMvc.perform(get("/actuator/httptraces")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.traceId == '%s')]", traceId).value(hasSize(1)))
            .andExpect(jsonPath("$[?(@.traceId == '%s')].method", traceId).value(contains("POST")))
            .andExpect(jsonPath("$[?(@.traceId == '%s')].url", traceId).value(contains("/cas/login?service=https://example.org")))
            .andExpect(jsonPath("$[?(@.traceId == '%s')].route", traceId).value(contains("/login")));
    }

    @Test
    void verifyDetailsOperation() throws Exception {
        val traceId = UUID.randomUUID().toString();
        localTraceStore.add(span(traceId, "span-2", "span-1", "child", "service-b", "INTERNAL", 200, 50,
            Map.of(), true));
        localTraceStore.add(span(traceId, "span-1", null, "root", "service-a", "SERVER", 100, 200,
            Map.of("http.target", "/cas/serviceValidate"), false));

        mockMvc.perform(get("/actuator/httptraces/{traceId}", traceId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.traceId").value(traceId))
            .andExpect(jsonPath("$.durationNanos").value(200))
            .andExpect(jsonPath("$.spans", hasSize(2)))
            .andExpect(jsonPath("$.spans[0].spanId").value("span-1"))
            .andExpect(jsonPath("$.spans[1].spanId").value("span-2"));
    }

    @Test
    void verifySummaryUsesServerSpanAndAggregatesTrace() {
        val store = new LocalTraceStore();
        store.add(span("trace-1", "span-2", "span-1", "repository", "service-b", "CLIENT", 30, 20,
            Map.of(), true));
        store.add(span("trace-1", "span-1", null, "HTTP GET", "service-a", "SERVER", 10, 100,
            Map.of("url.path", "/cas/login", "url.query", "renew=true",
                "http.route", "/login", "http.request.method", "GET"), false));

        val summaries = store.summaries();
        assertEquals(1, summaries.size());
        val summary = summaries.getFirst();
        assertEquals("trace-1", summary.traceId());
        assertEquals(Instant.ofEpochSecond(0, 10), summary.startedAt());
        assertEquals("HTTP GET", summary.rootSpan());
        assertEquals("GET", summary.method());
        assertEquals("/cas/login?renew=true", summary.url());
        assertEquals("/login", summary.route());
        assertEquals(Set.of("service-a", "service-b"), summary.services());
        assertEquals(2, summary.spanCount());
        assertEquals(100, summary.durationNanos());
        assertTrue(summary.error());
    }

    @Test
    void verifyDetailsAreSortedAndMissingTraceIsEmpty() {
        val store = new LocalTraceStore();
        store.add(span("trace-2", "span-2", "span-1", "second", "cas", "INTERNAL", 100, 10, Map.of(), false));
        store.add(span("trace-2", "span-1", null, "first", "cas", "SERVER", 10, 20, Map.of(), false));

        val detail = store.find("trace-2").orElseThrow();
        assertEquals("trace-2", detail.traceId());
        assertEquals(100, detail.durationNanos());
        assertEquals(List.of("span-1", "span-2"), detail.spans().stream().map(LocalSpan::spanId).toList());
        assertTrue(store.find("missing").isEmpty());
    }

    @Test
    void verifyParentSpanFallbacks() {
        val store = new LocalTraceStore();
        store.add(span("trace-3", "span-2", "span-1", "child", "service-b", "CLIENT", 10, 20, Map.of(), false));
        store.add(span("trace-3", "span-1", " ", "parent", "service-a", "INTERNAL", 20, 30,
            Map.of("http.method", "PUT", "http.target", "/cas/actuator"), false));

        val summary = store.summaries().getFirst();
        assertEquals("parent", summary.rootSpan());
        assertEquals("PUT", summary.method());
        assertEquals("/cas/actuator", summary.url());
        assertFalse(summary.error());
    }

    @Test
    void verifyFirstSpanAndAttributeFallbacks() {
        val store = new LocalTraceStore();
        store.add(span("trace-4", "span-1", "parent", "operation", "cas", "INTERNAL", 10, 10,
            Map.of("method", "PATCH", "http.url", "https://sso.example.org/cas/logout"), false));
        store.add(span("trace-4", "span-2", "parent", "other", "cas", "CLIENT", 20, 10, Map.of(), false));

        val summary = store.summaries().getFirst();
        assertEquals("operation", summary.rootSpan());
        assertEquals("PATCH", summary.method());
        assertEquals("https://sso.example.org/cas/logout", summary.url());
    }

    @ParameterizedTest
    @MethodSource("spanNameMethods")
    void verifyMethodsFromSpanName(final String spanName, final String expectedMethod) {
        val store = new LocalTraceStore();
        store.add(span("trace-" + UUID.randomUUID(), "span-1", null, spanName, "cas", "INTERNAL", 1, 1,
            Map.of("url.path", "/cas"), false));
        assertEquals(expectedMethod, store.summaries().getFirst().method());
    }

    @Test
    void verifyTraceEviction() {
        val store = new LocalTraceStore();
        for (var i = 0; i <= 1_000; i++) {
            store.add(span("trace-" + i, "span-" + i, null, "trace " + i, "cas", "SERVER", i, 1,
                Map.of(), false));
        }

        val summaries = store.summaries();
        assertEquals(1_000, summaries.size());
        assertEquals("trace-1000", summaries.getFirst().traceId());
        assertTrue(store.find("trace-0").isEmpty());
        assertTrue(store.find("trace-1000").isPresent());
        assertFalse(summaries.stream().anyMatch(summary -> "trace-0".equals(summary.traceId())));
    }

    private static Stream<Arguments> spanNameMethods() {
        return Stream.of(
            arguments("HTTP GET", "GET"),
            arguments("request POST", "POST"),
            arguments("http put", "PUT"),
            arguments("handler PATCH", "PATCH"),
            arguments("resource DELETE", "DELETE"),
            arguments("cors OPTIONS", "OPTIONS"),
            arguments("probe HEAD", "HEAD"),
            arguments("process", null),
            arguments(" ", null),
            arguments(null, null)
        );
    }

    private static LocalSpan span(final String traceId,
                                  final String spanId,
                                  @Nullable final String parentSpanId,
                                  final String name,
                                  final String serviceName,
                                  final String kind,
                                  final long startEpochNanos,
                                  final long durationNanos,
                                  final Map<String, String> attributes,
                                  final boolean error) {
        return new LocalSpan(traceId, spanId, parentSpanId, name,
            serviceName, kind, startEpochNanos,
            durationNanos, attributes, error);
    }
}
