package org.apereo.cas;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.api.IdentityProviderConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.security.SecureRandom;

/**
 * This is {@link BaseFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller
public abstract class BaseFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFederationRequestController.class);

    /**
     * Idp config service.
     */
    protected final IdentityProviderConfigurationService identityProviderConfigurationService;

    protected final ServicesManager servicesManager;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    protected final Service callbackService;

    protected final CasConfigurationProperties casProperties;
    
    public BaseFederationRequestController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                           final ServicesManager servicesManager,
                                           final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                           final CasConfigurationProperties casProperties) {
        this.identityProviderConfigurationService = identityProviderConfigurationService;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.casProperties = casProperties;
        this.callbackService = registerCallback("/ws/idp/federationcallback");
    }

    private Service registerCallback(final String callbackUrl) {
        final Service callbackService = this.webApplicationServiceFactory.createService(
                casProperties.getServer().getPrefix().concat(callbackUrl.concat(".+")));
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing callback service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(new SecureRandom().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("WS-Federation Authentication Request");
            service.setServiceId(callbackService.getId());

            LOGGER.debug("Saving callback service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
        return callbackService;
    }
}
