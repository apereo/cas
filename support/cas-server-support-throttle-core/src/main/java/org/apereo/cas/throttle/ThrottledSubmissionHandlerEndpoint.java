package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link ThrottledSubmissionHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuppressWarnings("removal")
@RestControllerEndpoint(id = "throttles", enableByDefault = false)
public class ThrottledSubmissionHandlerEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    public ThrottledSubmissionHandlerEndpoint(final CasConfigurationProperties casProperties,
                                              final ObjectProvider<AuthenticationThrottlingExecutionPlan> executionPlan) {
        super(casProperties);
        this.authenticationThrottlingExecutionPlan = executionPlan;
    }

    @GetMapping
    @Operation(summary = "Get throttled authentication records")
    public List getRecords() {
        return (List) authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors()
            .stream()
            .map(ThrottledSubmissionHandlerInterceptor.class::cast)
            .filter(Objects::nonNull)
            .map(ThrottledSubmissionHandlerInterceptor::getRecords)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Release throttled interceptors as necessary.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Clean and release throttled authentication records",
        parameters = @Parameter(name = "clear", required = false, description = "Whether to clear/remove the records or simply release them"))
    public void release(@RequestParam(value = "clear", required = false) final boolean clear) {
        val interceptors = authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors();
        interceptors
            .stream()
            .map(ThrottledSubmissionHandlerInterceptor.class::cast)
            .forEach(interceptor -> {
                if (clear) {
                    interceptor.clear();
                } else {
                    interceptor.release();
                }
            });
    }
}
