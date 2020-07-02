package org.apereo.cas.web.report;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;

import java.util.Collection;

/**
 * This is {@link RegisteredAuthenticationHandlersEndpoint}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@Endpoint(id = "authenticationHandlers", enableByDefault = false)
public class RegisteredAuthenticationHandlersEndpoint extends BaseCasActuatorEndpoint {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    public RegisteredAuthenticationHandlersEndpoint(
        final CasConfigurationProperties casProperties,
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {

        super(casProperties);
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
    }

    /**
     * Handle and produce a list of authn handlers from authentication event execution plan.
     *
     * @return the web async task
     */
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public Collection<AuthenticationHandler> handle() {
        return this.authenticationEventExecutionPlan.getAuthenticationHandlers();
    }

    /**
     * Fetch authn handler by name.
     *
     * @param name the name
     * @return the authentication handler
     */
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public AuthenticationHandler fetchAuthnHandler(@Selector final String name) {
        return this.authenticationEventExecutionPlan.getAuthenticationHandlers().stream().
            filter(authnHandler -> authnHandler.getName().equals(name)).
            findFirst().orElse(null);
    }
}
