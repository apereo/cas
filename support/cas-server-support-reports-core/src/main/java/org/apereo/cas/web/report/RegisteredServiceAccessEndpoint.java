package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.apereo.cas.web.support.ArgumentExtractor;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceAccessEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RestControllerEndpoint(id = "serviceAccess", enableByDefault = false)
@Slf4j
public class RegisteredServiceAccessEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<ServicesManager> servicesManager;
    private final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;
    private final ObjectProvider<ArgumentExtractor> argumentExtractor;
    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;
    private final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;
    private final ObjectProvider<PrincipalResolver> principalResolver;
    private final ObjectProvider<PrincipalFactory> principalFactory;

    public RegisteredServiceAccessEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan,
        final ObjectProvider<ArgumentExtractor> argumentExtractor,
        final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
        final ObjectProvider<PrincipalResolver> principalResolver,
        final ObjectProvider<PrincipalFactory> principalFactory) {
        super(casProperties);
        this.authenticationServiceSelectionPlan = authenticationServiceSelectionPlan;
        this.servicesManager = servicesManager;
        this.argumentExtractor = argumentExtractor;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.principalResolver = principalResolver;
        this.principalFactory = principalFactory;
    }

    @GetMapping(
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML,
            MediaType.APPLICATION_JSON_VALUE
        })
    @Operation(summary = "Verify if service access can be granted to the user")
    public ResponseEntity authorize(final HttpServletRequest request) {
        try {
            val service = authenticationServiceSelectionPlan.getObject().resolveService(argumentExtractor.getObject().extractService(request));
            val registeredService = servicesManager.getObject().findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            val authentication = buildAuthentication(request.getParameter("username"), request.getParameter("password"), service);
            val accessRequest = AuditableContext
                .builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(accessRequest);
            return accessResult.isExecutionFailure()
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access to %s is denied".formatted(service.getId()))
                : ResponseEntity.ok(Map.of("registeredService", registeredService, "authentication", authentication, "service", service));
        } catch (final AuthenticationException e) {
            LoggingUtils.warn(LOGGER, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private Authentication buildAuthentication(final String username, final String password,
                                               final Service selectedService) throws Throwable {
        if (StringUtils.isNotBlank(password)) {
            val credential = new UsernamePasswordCredential(username, password);
            val result = authenticationSystemSupport.getObject().finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = principalResolver.getObject().resolve(new BasicIdentifiableCredential(username),
            Optional.of(principalFactory.getObject().createPrincipal(username)),
            Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

}
