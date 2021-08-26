package org.apereo.cas.web.report;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

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
    @Operation(summary = "Get collection of available authentication handlers")
    public Collection<AuthenticationHandlerDetails> handle() {
        return this.authenticationEventExecutionPlan.getAuthenticationHandlers()
            .stream()
            .map(handler -> AuthenticationHandlerDetails.builder().name(handler.getName()).order(handler.getOrder()).build())
            .sorted(Comparator.comparing(AuthenticationHandlerDetails::getOrder))
            .collect(Collectors.toList());
    }

    /**
     * Fetch authn handler by name.
     *
     * @param name the name
     * @return the authentication handler
     */
    @Operation(summary = "Get available authentication handler by name", parameters = {@Parameter(name = "name", required = true)})
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    public AuthenticationHandlerDetails fetchAuthnHandler(@Selector final String name) {
        return this.authenticationEventExecutionPlan.getAuthenticationHandlers()
            .stream()
            .filter(authnHandler -> authnHandler.getName().equalsIgnoreCase(name))
            .findFirst()
            .map(handler -> AuthenticationHandlerDetails.builder().name(handler.getName()).order(handler.getOrder()).build())
            .orElse(null);
    }

    @SuperBuilder
    @Getter
    private static class AuthenticationHandlerDetails implements Serializable {
        private static final long serialVersionUID = 6755362844006190415L;

        private final String name;

        private final Integer order;
    }

}
