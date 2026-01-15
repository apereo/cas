package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.MediaType;

/**
 * This is {@link RegisteredAuthenticationPoliciesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "authenticationPolicies", defaultAccess = Access.NONE)
public class RegisteredAuthenticationPoliciesEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<@NonNull AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    public RegisteredAuthenticationPoliciesEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<@NonNull AuthenticationEventExecutionPlan> authenticationEventExecutionPlan) {

        super(casProperties);
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
    }

    /**
     * Handle and produce a list of authn policy from authentication event execution plan.
     *
     * @return the web async task
     */
    @ReadOperation(produces = {
        MediaType.APPLICATION_JSON_VALUE, MEDIA_TYPE_SPRING_BOOT_V2_JSON, MEDIA_TYPE_CAS_YAML})
    @Operation(summary = "Get available authentication policies")
    public Collection<AuthenticationPolicyDetails> handle() {
        return this.authenticationEventExecutionPlan.getObject().getAuthenticationPolicies()
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
        MediaType.APPLICATION_JSON_VALUE, MEDIA_TYPE_SPRING_BOOT_V2_JSON, MEDIA_TYPE_CAS_YAML})
    @Operation(summary = "Get available authentication policy by name", parameters = @Parameter(name = "name", required = true, description = "The name of the policy to fetch"))
    public AuthenticationPolicyDetails fetchPolicy(@Selector final String name) {
        return this.authenticationEventExecutionPlan.getObject().getAuthenticationPolicies()
            .stream()
            .filter(authnHandler -> authnHandler.getName().equals(name))
            .findFirst()
            .map(policy -> AuthenticationPolicyDetails.builder().name(policy.getName()).order(policy.getOrder()).build())
            .orElse(null);
    }

    @SuperBuilder
    @Getter
    @SuppressWarnings("UnusedMethod")
    private static final class AuthenticationPolicyDetails implements Serializable {
        @Serial
        private static final long serialVersionUID = 6755362844006190415L;

        private final String name;

        private final Integer order;
    }
}
