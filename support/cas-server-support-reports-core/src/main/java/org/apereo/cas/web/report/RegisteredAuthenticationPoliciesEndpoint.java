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
    @Operation(summary = "Get available authentication policies")
    public Collection<AuthenticationPolicyDetails> handle() {
        return this.authenticationEventExecutionPlan.getAuthenticationPolicies()
            .stream()
            .map(policy -> AuthenticationPolicyDetails.builder().name(policy.getName()).order(policy.getOrder()).build())
            .sorted(Comparator.comparing(AuthenticationPolicyDetails::getOrder))
            .collect(Collectors.toList());
    }

    /**
     * Fetch authn policy by name.
     *
     * @param name the name
     * @return the authentication policy
     */
    @ReadOperation(produces = {
        ActuatorMediaType.V2_JSON, "application/vnd.cas.services+yaml", MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get available authentication policy by name", parameters = {@Parameter(name = "name", required = true)})
    public AuthenticationPolicyDetails fetchPolicy(@Selector final String name) {
        return this.authenticationEventExecutionPlan.getAuthenticationPolicies()
            .stream()
            .filter(authnHandler -> authnHandler.getName().equals(name))
            .findFirst()
            .map(policy -> AuthenticationPolicyDetails.builder().name(policy.getName()).order(policy.getOrder()).build())
            .orElse(null);
    }

    @SuperBuilder
    @Getter
    private static class AuthenticationPolicyDetails implements Serializable {
        private static final long serialVersionUID = 6755362844006190415L;

        private final String name;

        private final Integer order;
    }
}
