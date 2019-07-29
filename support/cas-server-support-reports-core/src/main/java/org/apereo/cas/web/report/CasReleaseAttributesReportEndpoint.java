package org.apereo.cas.web.report;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.validation.ImmutableAssertion;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasReleaseAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Endpoint(id = "releaseAttributes", enableByDefault = false)
public class CasReleaseAttributesReportEndpoint extends BaseCasActuatorEndpoint {
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
     */
    @ReadOperation
    public Map<String, Object> releasePrincipalAttributes(final String username,
                                                          final String password,
                                                          final String service) {


        val selectedService = this.serviceFactory.createService(service);
        val registeredService = this.servicesManager.findServiceBy(selectedService);

        val credential = new UsernamePasswordCredential(username, password);
        val result = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, credential);
        val authentication = result.getAuthentication();

        val principal = authentication.getPrincipal();
        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(principal, selectedService, registeredService);
        val builder = DefaultAuthenticationBuilder.of(
                principal,
                this.principalFactory,
                attributesToRelease,
                selectedService,
                registeredService,
                authentication);

        val finalAuthentication = builder.build();
        val assertion = new DefaultAssertionBuilder(finalAuthentication)
            .with(selectedService)
            .with(CollectionUtils.wrap(finalAuthentication))
            .build();

        val resValidation = new LinkedHashMap<String, Object>();
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        resValidation.put("registeredService", registeredService);

        return resValidation;
    }

    /**
     * Method that accepts a JSON body through a POST method to receive user credentials and only returns a
     * map of attributes released for the authenticated user.
     *
     * @param username - the username
     * @param password - the password
     * @param service - the service id
     * @return - the map
     */
    @WriteOperation
    public Map<String, Object> releaseAttributes(final String username,
                                                 final String password,
                                                 final String service) {
        val map = releasePrincipalAttributes(username, password, service);
        val assertion = (ImmutableAssertion) map.get("assertion");
        return Map.of("uid", username, "attributes", assertion.getPrimaryAuthentication().getPrincipal().getAttributes());
    }
}
