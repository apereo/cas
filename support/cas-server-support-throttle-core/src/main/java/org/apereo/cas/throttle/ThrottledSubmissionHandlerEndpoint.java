package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * This is {@link ThrottledSubmissionHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "throttles", defaultAccess = Access.NONE)
public class ThrottledSubmissionHandlerEndpoint extends BaseCasRestActuatorEndpoint {

    private final ObjectProvider<@NonNull AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;
    private final ObjectProvider<@NonNull ThrottledSubmissionsStore> throttledSubmissionsStore;

    public ThrottledSubmissionHandlerEndpoint(final CasConfigurationProperties casProperties,
                                              final ConfigurableApplicationContext applicationContext,
                                              final ObjectProvider<@NonNull AuthenticationThrottlingExecutionPlan> executionPlan,
                                              final ObjectProvider<@NonNull ThrottledSubmissionsStore> throttledSubmissionsStore) {
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
     * Delete by key or release throttled interceptors as necessary.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Clean or release all throttled interceptors or remove a throttled authentication record by key",
            parameters = { @Parameter(name = "clear", required = false, description = "Whether to clear/remove the records or simply release them"),
                           @Parameter(name = "key", required = false, description = "Selected key for removal")})
    public void deleteByKeyOrRelease(@RequestParam(value = "clear", required = false) final boolean clear,
                                     @RequestParam(value = "key", required = false) final String key) {
        if (StringUtils.isNotBlank(key)) {
            throttledSubmissionsStore.getObject().remove(key);
        } else {
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
}
