package org.apereo.cas.web;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.authentication.TokenAuthenticationSecurity;
import org.apereo.cas.util.CollectionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link TokenAuthenticationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "tokenAuth", defaultAccess = Access.NONE)
public class TokenAuthenticationEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<PrincipalResolver> principalResolver;
    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory;

    private final ObjectProvider<PrincipalFactory> principalFactory;

    public TokenAuthenticationEndpoint(
        final ObjectProvider<CasConfigurationProperties> casProperties,
        final ObjectProvider<PrincipalResolver> principalResolver,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory,
        final ObjectProvider<PrincipalFactory> principalFactory) {
        super(casProperties.getObject());
        this.principalResolver = principalResolver;
        this.servicesManager = servicesManager;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
    }

    /**
     * Produce token for user and service.
     *
     * @param username the username
     * @param service  the service
     * @return the map
     * @throws Throwable the throwable
     */
    @WriteOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Produce an authentication token for the user and the intended application", parameters = {
        @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to authenticate"),
        @Parameter(name = "service", required = true, in = ParameterIn.QUERY, description = "May be the service id or its numeric identifier")
    })
    public Map<?, ?> produceToken(@Selector final String username,
                                  final String service) throws Throwable {
        val selectedService = serviceFactory.getObject().createService(service);
        val registeredService = NumberUtils.isCreatable(service)
            ? servicesManager.getObject().findServiceBy(Long.parseLong(service))
            : servicesManager.getObject().findServiceBy(selectedService);
        val principal = principalResolver.getObject().resolve(new BasicIdentifiableCredential(username),
            Optional.of(principalFactory.getObject().createPrincipal(username)),
            Optional.empty(), Optional.of(selectedService));
        val authentication = DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
        val audit = AuditableContext.builder()
            .service(selectedService)
            .authentication(authentication)
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
        accessResult.throwExceptionIfNeeded();
        val token = TokenAuthenticationSecurity.forRegisteredService(registeredService).generateTokenFor(authentication);
        return Map.of("registeredService", registeredService, "token", token);
    }

    /**
     * Validate token and return results.
     *
     * @param token   the token
     * @param service the service
     * @return the map
     * @throws Throwable the throwable
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate an authentication token for the user and the intended application", parameters = {
        @Parameter(name = "token", required = true, in = ParameterIn.PATH, description = "The token to validate"),
        @Parameter(name = "service", required = true, in = ParameterIn.QUERY, description = "May be the service id or its numeric identifier")
    })
    public Map<?, ?> validateToken(@Selector final String token,
                                  final String service) throws Throwable {
        val selectedService = serviceFactory.getObject().createService(service);
        val registeredService = NumberUtils.isCreatable(service)
            ? servicesManager.getObject().findServiceBy(Long.parseLong(service))
            : servicesManager.getObject().findServiceBy(selectedService);
        val profile = TokenAuthenticationSecurity.forRegisteredService(registeredService).validateToken(token);
        Assert.notNull(profile, "Authentication attempt failed to produce an authenticated profile");
        val attributes = CollectionUtils.toMultiValuedMap(profile.getAttributes());
        val principal = principalFactory.getObject().createPrincipal(profile.getId(), attributes);
        val audit = AuditableContext.builder()
            .service(selectedService)
            .principal(principal)
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
        accessResult.throwExceptionIfNeeded();
        return Map.of("registeredService", registeredService, "principal", principal);
    }
}
