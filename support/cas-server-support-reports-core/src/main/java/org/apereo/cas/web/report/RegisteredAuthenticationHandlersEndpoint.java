package org.apereo.cas.web.report;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.MediaType;

import java.io.Serial;
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
@Endpoint(id = "authenticationHandlers", defaultAccess = Access.NONE)
public class RegisteredAuthenticationHandlersEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    public RegisteredAuthenticationHandlersEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan) {

        super(casProperties);
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
    }

    /**
     * Handle and produce a list of authn handlers from authentication event execution plan.
     *
     * @return the web async task
     */
    @ReadOperation(produces = {
        MEDIA_TYPE_SPRING_BOOT_V2_JSON, MEDIA_TYPE_CAS_YAML, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get collection of available authentication handlers")
    public Collection<AuthenticationHandlerDetails> handle() {
        return authenticationEventExecutionPlan.getObject().resolveAuthenticationHandlers()
            .stream()
            .map(RegisteredAuthenticationHandlersEndpoint::buildHandlerDetails)
            .sorted(Comparator.comparing(AuthenticationHandlerDetails::getOrder))
            .collect(Collectors.toList());
    }

    /**
     * Fetch authn handler by name.
     *
     * @param name the name
     * @return the authentication handler
     */
    @Operation(summary = "Get available authentication handler by name", parameters = @Parameter(name = "name", required = true, description = "The handler name"))
    @ReadOperation(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    public AuthenticationHandlerDetails fetchAuthnHandler(@Selector final String name) {
        return authenticationEventExecutionPlan.getObject()
            .resolveAuthenticationHandlers()
            .stream()
            .filter(authnHandler -> authnHandler.getName().equalsIgnoreCase(name))
            .findFirst()
            .map(RegisteredAuthenticationHandlersEndpoint::buildHandlerDetails)
            .orElse(null);
    }

    private static AuthenticationHandlerDetails buildHandlerDetails(final AuthenticationHandler handler) {
        return AuthenticationHandlerDetails.builder()
            .name(handler.getName())
            .type(handler.getClass().getName())
            .order(handler.getOrder())
            .state(handler.getState().name())
            .build();
    }

    @SuperBuilder
    @Getter
    @SuppressWarnings("UnusedMethod")
    public static final class AuthenticationHandlerDetails implements Serializable {
        @Serial
        private static final long serialVersionUID = 6755362844006190415L;

        private final String name;

        private final String type;

        private final Integer order;

        private final String state;
    }

}
