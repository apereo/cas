package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.authentication.TokenAuthenticationSecurity;
import org.apereo.cas.util.CollectionUtils;
import com.google.common.primitives.Longs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.http.MediaType;

/**
 * This is {@link TokenAuthenticationEndpoint}. It validates a JWT login token for a service and answers the principal
 * it authenticates, with the service's identifier, name and service id; the service's secrets are never part of
 * the answer.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "tokenAuth", defaultAccess = Access.NONE)
public class TokenAuthenticationEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory;

    private final ObjectProvider<PrincipalFactory> principalFactory;

    public TokenAuthenticationEndpoint(
        final ObjectProvider<CasConfigurationProperties> casProperties,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory,
        final ObjectProvider<PrincipalFactory> principalFactory) {
        super(casProperties.getObject());
        this.servicesManager = servicesManager;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
    }

    /**
     * Validate token and return results. A token that does not validate for the service is answered with {@code 400}.
     *
     * @param token   the token
     * @param service the service
     * @return the identifier, name and service id of the registered service, and the principal
     * @throws Throwable the throwable
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate an authentication token for the user and the intended application", parameters = {
        @Parameter(name = "token", required = true, in = ParameterIn.PATH, description = "The token to validate"),
        @Parameter(name = "service", required = true, in = ParameterIn.QUERY, description = "May be the service id or its numeric identifier")
    })
    public WebEndpointResponse<Map<String, ?>> validateToken(@Selector final String token,
                                                             final String service)
            throws Throwable {
        val selectedService = serviceFactory.getObject().createService(service);
        val registeredServiceId = Longs.tryParse(service);
        val registeredService = registeredServiceId != null
            ? servicesManager.getObject().findServiceBy(registeredServiceId)
            : servicesManager.getObject().findServiceBy(selectedService);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val validatedToken = TokenAuthenticationSecurity.forRegisteredService(Objects.requireNonNull(registeredService))
            .validateToken(token);
        if (validatedToken.isEmpty()) {
            return new WebEndpointResponse<>(Map.of("error", "Authentication attempt failed to validate the token"),
                WebEndpointResponse.STATUS_BAD_REQUEST);
        }
        val authenticatedToken = validatedToken.get();
        val attributes = CollectionUtils.toMultiValuedMap(authenticatedToken.claims());
        val principal = Objects.requireNonNull(principalFactory.getObject().createPrincipal(authenticatedToken.subject(), attributes));
        val audit = AuditableContext.builder()
            .service(selectedService)
            .principal(principal)
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
        accessResult.throwExceptionIfNeeded();
        val serviceSummary = CollectionUtils.wrap("id", registeredService.getId(),
            "name", registeredService.getName(), "serviceId", registeredService.getServiceId());
        return new WebEndpointResponse<>(Map.of("registeredService", serviceSummary, "principal", principal));
    }
}
