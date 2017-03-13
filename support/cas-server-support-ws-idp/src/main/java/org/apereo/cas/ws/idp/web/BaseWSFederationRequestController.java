package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.security.SecureRandom;

/**
 * This is {@link BaseWSFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller
public abstract class BaseWSFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWSFederationRequestController.class);

    /**
     * Idp config service.
     */
    protected final IdentityProviderConfigurationService identityProviderConfigurationService;

    protected final ServicesManager servicesManager;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    protected final Service callbackService;

    protected final CasConfigurationProperties casProperties;

    protected final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;
    
    protected final HttpClient httpClient;

    public BaseWSFederationRequestController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                             final ServicesManager servicesManager,
                                             final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                             final CasConfigurationProperties casProperties,
                                             final AuthenticationServiceSelectionStrategy serviceSelectionStrategy, 
                                             final HttpClient httpClient) {
        this.identityProviderConfigurationService = identityProviderConfigurationService;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.casProperties = casProperties;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
        this.httpClient = httpClient;
        this.callbackService = registerCallback(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK);
    }

    private Service registerCallback(final String callbackUrl) {
        final Service callbackService = this.webApplicationServiceFactory.createService(callbackUrl);
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing callback service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(new SecureRandom().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("WS-Federation Authentication Request");
            service.setServiceId(callbackService.getId().concat(".+"));

            LOGGER.debug("Saving callback service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
        return callbackService;
    }

    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                         final WSFederationRequest WSFederationRequest) {
        try {
            final URIBuilder builder = new URIBuilder(this.callbackService.getId());

            builder.addParameter(WSFederationConstants.WA, WSFederationRequest.getWa());
            builder.addParameter(WSFederationConstants.WREPLY, WSFederationRequest.getWreply());
            builder.addParameter(WSFederationConstants.WTREALM, WSFederationRequest.getWtrealm());

            if (StringUtils.isNotBlank(WSFederationRequest.getWctx())) {
                builder.addParameter(WSFederationConstants.WCTX, WSFederationRequest.getWctx());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWfresh())) {
                builder.addParameter(WSFederationConstants.WREFRESH, WSFederationRequest.getWfresh());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWhr())) {
                builder.addParameter(WSFederationConstants.WHR, WSFederationRequest.getWhr());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWreq())) {
                builder.addParameter(WSFederationConstants.WREQ, WSFederationRequest.getWreq());
            }

            final URI url = builder.build();

            LOGGER.debug("Built service callback url [{}]", url);
            return org.jasig.cas.client.util.CommonUtils.constructServiceUrl(request, response,
                    url.toString(), casProperties.getServer().getName(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

}
