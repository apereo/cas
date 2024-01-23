package org.apereo.cas.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
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

    private final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    public ThrottledSubmissionHandlerEndpoint(final CasConfigurationProperties casProperties,
                                              final ObjectProvider<AuthenticationThrottlingExecutionPlan> executionPlan) {
        super(casProperties);
        this.authenticationThrottlingExecutionPlan = executionPlan;
    }

    @ReadOperation
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
    @DeleteOperation
    @Operation(summary = "Clean and release throttled authentication records")
    public void release() {
        val interceptors = authenticationThrottlingExecutionPlan.getObject().getAuthenticationThrottleInterceptors();
        interceptors
            .stream()
            .map(ThrottledSubmissionHandlerInterceptor.class::cast)
            .forEach(ThrottledSubmissionHandlerInterceptor::release);
    }
}
