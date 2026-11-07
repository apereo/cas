package org.apereo.cas.throttle;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link ThrottledSubmissionHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "throttles", defaultAccess = Access.NONE)
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

    /**
     * Get throttled authentication records, all of them or only those submitted for one username.
     * The username is matched regardless of case.
     *
     * @param username the username whose records are returned; blank returns every record
     * @return the records
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get throttled authentication records, optionally only those of a username",
        parameters = @Parameter(name = "username", required = false,
            description = "Return only the records submitted for this username, matched regardless of case"))
    public List<ThrottledSubmission> getRecords(@RequestParam(value = "username", required = false) final @Nullable String username) {
        final Stream<?> entries = throttledSubmissionsStore.getObject().entries();
        return entries
            .map(ThrottledSubmission.class::cast)
            .filter(submission -> StringUtils.isBlank(username) || Strings.CI.equals(username, submission.getUsername()))
            .toList();
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
