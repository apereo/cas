package org.apereo.cas.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

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
@Endpoint(id = "throttles", enableByDefault = false)
public class ThrottledSubmissionHandlerEndpoint extends BaseCasActuatorEndpoint {

    private final AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan;

    public ThrottledSubmissionHandlerEndpoint(final CasConfigurationProperties casProperties,
        final AuthenticationThrottlingExecutionPlan executionPlan) {
        super(casProperties);
        this.authenticationThrottlingExecutionPlan = executionPlan;
    }

    @ReadOperation
    @Operation(summary = "Get throttled authentication records")
    public List getRecords() {
        return (List) authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors()
            .stream()
            .map(entry -> (ThrottledSubmissionHandlerInterceptor) entry)
            .filter(Objects::nonNull)
            .map(ThrottledSubmissionHandlerInterceptor::getRecords)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
