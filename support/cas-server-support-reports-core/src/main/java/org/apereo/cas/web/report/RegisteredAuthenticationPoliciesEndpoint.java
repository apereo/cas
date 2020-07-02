package org.apereo.cas.web.report;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;

import java.util.Collection;

/**
 * This is {@link RegisteredAuthenticationPoliciesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "authenticationPolicies", enableByDefault = false)
public class RegisteredAuthenticationPoliciesEndpoint extends BaseCasActuatorEndpoint {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    public RegisteredAuthenticationPoliciesEndpoint(
        final CasConfigurationProperties casProperties,
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {

        super(casProperties);
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
    }

    /**
     * Handle and produce a list of authn policy from authentication event execution plan.
     *
     * @return the web async task
     */
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public Collection<AuthenticationPolicy> handle() {
        return this.authenticationEventExecutionPlan.getAuthenticationPolicies();
    }

    /**
     * Fetch authn policy by name.
     *
     * @param name the name
     * @return the authentication policy
     */
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public AuthenticationPolicy fetchPolicy(@Selector final String name) {
        return this.authenticationEventExecutionPlan.getAuthenticationPolicies().stream().
            filter(authnHandler -> authnHandler.getName().equals(name)).
            findFirst().orElse(null);
    }
}
