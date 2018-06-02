package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasReleaseAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Endpoint(id = "release-attributes", enableByDefault = false)
public class CasReleaseAttributesReportEndpoint extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServiceFactory<WebApplicationService> serviceFactory;
    private final PrincipalFactory principalFactory;

    public CasReleaseAttributesReportEndpoint(final CasConfigurationProperties casProperties,
                                              final ServicesManager servicesManager,
                                              final AuthenticationSystemSupport authenticationSystemSupport,
                                              final ServiceFactory<WebApplicationService> serviceFactory,
                                              final PrincipalFactory principalFactory) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
    }

    /**
     * Release principal attributes map.
     *
     * @param username the username
     * @param password the password
     * @param service  the service
     * @return the map
     * @throws Exception the exception
     */
    @ReadOperation
    public Map<String, Object> releasePrincipalAttributes(final String username,
                                                          final String password,
                                                          final String service) throws Exception {


        final Service selectedService = this.serviceFactory.createService(service);
        final var registeredService = this.servicesManager.findServiceBy(selectedService);

        final var credential = new UsernamePasswordCredential(username, password);
        final var result = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, credential);
        final var authentication = result.getAuthentication();

        final var principal = authentication.getPrincipal();
        final var attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(principal, selectedService, registeredService);
        final var principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, selectedService, registeredService);
        final var modifiedPrincipal = this.principalFactory.createPrincipal(principalId, attributesToRelease);
        final var builder = DefaultAuthenticationBuilder.newInstance(authentication);
        builder.setPrincipal(modifiedPrincipal);
        final var finalAuthentication = builder.build();
        final var assertion = new DefaultAssertionBuilder(finalAuthentication)
            .with(selectedService)
            .with(CollectionUtils.wrap(finalAuthentication))
            .build();

        final var resValidation = new LinkedHashMap<String, Object>();
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        resValidation.put("registeredService", registeredService);

        return resValidation;
    }
}
