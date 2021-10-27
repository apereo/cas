package org.jasig.cas.support.oauth;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OAuthServletContextListener extends AbstractServletContextInitializer {

    @Value("${server.prefix:http://localhost:8080/cas}" + OAuthConstants.ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE)
    private String callbackAuthorizeUrl;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(OAuthConstants.ENDPOINT_OAUTH2, "oauth20WrapperController");

        final ReloadableServicesManager servicesManager = getServicesManager();
        final Service callbackService = webApplicationServiceFactory.createService(this.callbackAuthorizeUrl);
        if (!servicesManager.matchesExistingService(callbackService))  {
            final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
            service.setName("OAuth Callback url");
            service.setDescription("OAuth Wrapper Callback Url");
            service.setServiceId(this.callbackAuthorizeUrl);

            addRegisteredServiceToServicesManager(service);
            servicesManager.reload();
        }

    }

    @Override
    protected void initializeServletContext(final ServletContextEvent event) {
        if (WebUtils.isCasServletInitializing(event)) {
            addEndpointMappingToCasServlet(event, OAuthConstants.ENDPOINT_OAUTH2);
        }
    }
}
