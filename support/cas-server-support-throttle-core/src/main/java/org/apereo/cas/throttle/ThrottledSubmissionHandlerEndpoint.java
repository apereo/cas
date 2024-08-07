package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * This is {@link ThrottledSubmissionHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "throttles", enableByDefault = false)
public class ThrottledSubmissionHandlerEndpoint extends BaseCasRestActuatorEndpoint {

    private final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;
    private final ObjectProvider<ThrottledSubmissionsStore> throttledSubmissionsStore;

    public ThrottledSubmissionHandlerEndpoint(final CasConfigurationProperties casProperties,
                                              final ConfigurableApplicationContext applicationContext,
                                              final ObjectProvider<AuthenticationThrottlingExecutionPlan> executionPlan,
                                              final ObjectProvider<ThrottledSubmissionsStore> throttledSubmissionsStore) {
        super(casProperties, applicationContext);
        this.authenticationThrottlingExecutionPlan = executionPlan;
        this.throttledSubmissionsStore = throttledSubmissionsStore;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get throttled authentication records")
    public List<ThrottledSubmission> getRecords() {
        return throttledSubmissionsStore.getObject().entries().toList();
    }

    /**
     * Delete by key.
     *
     * @param key the key
     */
    @DeleteMapping(path= "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Removed selected key from the throttled authentication records")
    public void deleteByKey(@PathVariable final String key) {
        throttledSubmissionsStore.getObject().remove(key);
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
