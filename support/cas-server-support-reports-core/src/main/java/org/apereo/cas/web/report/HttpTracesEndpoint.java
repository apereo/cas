package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.tracing.LocalTraceDetail;
import org.apereo.cas.tracing.LocalTraceStore;
import org.apereo.cas.tracing.LocalTraceSummary;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

/**
 * This is {@link HttpTracesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Endpoint(id = "httptraces", defaultAccess = Access.NONE)
public class HttpTracesEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<LocalTraceStore> store;

    public HttpTracesEndpoint(final CasConfigurationProperties casProperties,
                              final ObjectProvider<LocalTraceStore> store) {
        super(casProperties);
        this.store = store;
    }

    /**
     * Summaries list.
     *
     * @return the list
     */
    @ReadOperation
    @Operation(summary = "Get a list of local trace summaries",
            description = "This endpoint returns a list of local trace summaries. "
                + "Each summary provides basic information about a trace, such as its ID, timestamp, and duration.")
    public List<LocalTraceSummary> summaries() {
        return store.getObject().summaries();
    }

    /**
     * Details for a span id.
     *
     * @param traceId the trace id
     * @return local trace detail
     */
    @ReadOperation
    @Operation(summary = "Get detailed information about a specific trace",
            description = "This endpoint returns detailed information about a specific trace identified by its trace ID. "
                + "The details include the trace's spans, events, and other relevant data.")
    public LocalTraceDetail details(@Selector final String traceId) {
        return store.getObject().find(traceId).orElseThrow();
    }
}
