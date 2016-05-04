package org.jasig.cas;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.validation.ValidationServiceSelectionStrategy;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Initializes the CAS root servlet context to make sure
 * OAuth endpoint can be activated by the main CAS servlet.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("oauthApplicationContextWrapper")
public class OAuthApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Value("${server.prefix:http://localhost:8080/cas}")
    private String casServerUrl;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("oauth20ValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy oauth20ValidationServiceSelectionStrategy;

    @Resource(name = "validationServiceSelectionStrategies")
    private List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies;

    /**
     * Initialize servlet application context.
     */
    @PostConstruct
    public void initializeServletApplicationContext() {
        final String oAuthCallbackUrl = this.casServerUrl + OAuthConstants.BASE_OAUTH20_URL + '/'
                + OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION;
        final ReloadableServicesManager servicesManager = getServicesManager();
        final Service callbackService = this.webApplicationServiceFactory.createService(oAuthCallbackUrl);
        
        final RegisteredService svc = servicesManager.findServiceBy(callbackService);
        
        if (svc != null && !svc.getServiceId().equals(oAuthCallbackUrl)) {
            final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
            service.setName("OAuth Callback url");
            service.setDescription("OAuth Wrapper Callback Url");
            service.setServiceId(oAuthCallbackUrl);
            service.setEvaluationOrder(Integer.MIN_VALUE);
            
            addRegisteredServiceToServicesManager(service);
            servicesManager.reload();
        }

        this.validationServiceSelectionStrategies.add(0, this.oauth20ValidationServiceSelectionStrategy);
    }
}
