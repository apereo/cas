package org.jasig.cas;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jasig.cas.support.oauth.ticket.registry.AccessTokenDelegator;
import org.jasig.cas.support.oauth.ticket.registry.OAuthCodeDelegator;
import org.jasig.cas.support.oauth.ticket.registry.RefreshTokenDelegator;
import org.jasig.cas.ticket.registry.AbstractTicketDelegator;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.util.Pair;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component
public class OAuthApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Value("${server.prefix:http://localhost:8080/cas}")
    private String casServerUrl;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    private AbstractTicketRegistry ticketRegistry;

    /**
     * Initialize servlet application context.
     */
    @PostConstruct
    public void initializeServletApplicationContext() {
        final String oAuthCallbackUrl = casServerUrl + OAuthConstants.BASE_OAUTH20_URL + '/'
                + OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION;
        final ReloadableServicesManager servicesManager = getServicesManager();
        final Service callbackService = webApplicationServiceFactory.createService(oAuthCallbackUrl);
        if (!servicesManager.matchesExistingService(callbackService)) {
            final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
            service.setName("OAuth Callback url");
            service.setDescription("OAuth Wrapper Callback Url");
            service.setServiceId(oAuthCallbackUrl);

            addRegisteredServiceToServicesManager(service);
            servicesManager.reload();
        }

        final List delegators = ticketRegistry.getTicketDelegators();
        if (delegators != null) {
            ticketRegistry.getTicketDelegators().add(0, new Pair(RefreshToken.class,
                    AbstractTicketDelegator.getDefaultConstructor(RefreshTokenDelegator.class)));
            ticketRegistry.getTicketDelegators().add(1, new Pair(AccessToken.class,
                    AbstractTicketDelegator.getDefaultConstructor(AccessTokenDelegator.class)));
            ticketRegistry.getTicketDelegators().add(2, new Pair(OAuthCode.class,
                    AbstractTicketDelegator.getDefaultConstructor(OAuthCodeDelegator.class)));
        } else {
            throw new RuntimeException("Ticket registry delegators cannot be determined");
        }
    }
}
