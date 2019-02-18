package org.apereo.cas.support.saml.web;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SamlValidateEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Endpoint(id = "samlValidate", enableByDefault = false)
public class SamlValidateEndpoint extends BaseCasActuatorEndpoint {
    private final ServicesManager servicesManager;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServiceFactory<WebApplicationService> serviceFactory;
    private final PrincipalFactory principalFactory;
    private final SamlResponseBuilder samlResponseBuilder;
    private final OpenSamlConfigBean openSamlConfigBean;

    public SamlValidateEndpoint(final CasConfigurationProperties casProperties,
                                final ServicesManager servicesManager,
                                final AuthenticationSystemSupport authenticationSystemSupport,
                                final ServiceFactory<WebApplicationService> serviceFactory,
                                final PrincipalFactory principalFactory,
                                final SamlResponseBuilder samlResponseBuilder,
                                final OpenSamlConfigBean openSamlConfigBean) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.samlResponseBuilder = samlResponseBuilder;
        this.openSamlConfigBean = openSamlConfigBean;
    }

    /**
     * Handle validation request and produce saml1 payload.
     *
     * @param username the username
     * @param password the password
     * @param service  the service
     * @return the map
     */
    @ReadOperation
    public Map<String, Object> handle(final String username,
                                      final String password,
                                      final String service) {

        val selectedService = this.serviceFactory.createService(service);
        val registeredService = this.servicesManager.findServiceBy(selectedService);

        val credential = new UsernamePasswordCredential(username, password);
        val result = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, credential);
        val authentication = result.getAuthentication();

        val principal = authentication.getPrincipal();
        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(principal, selectedService, registeredService);
        val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, selectedService, registeredService);
        val modifiedPrincipal = this.principalFactory.createPrincipal(principalId, attributesToRelease);
        val builder = DefaultAuthenticationBuilder.newInstance(authentication);
        builder.setPrincipal(modifiedPrincipal);
        val finalAuthentication = builder.build();

        val samlResponse = this.samlResponseBuilder.createResponse(selectedService.getId(), selectedService);
        samlResponseBuilder.prepareSuccessfulResponse(samlResponse, selectedService, finalAuthentication, principal,
            finalAuthentication.getAttributes(), principal.getAttributes());

        val resValidation = new LinkedHashMap<String, Object>();
        val encoded = SamlUtils.transformSamlObject(this.openSamlConfigBean, samlResponse).toString();
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, encoded);
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        resValidation.put("registeredService", registeredService);

        return resValidation;
    }
}
