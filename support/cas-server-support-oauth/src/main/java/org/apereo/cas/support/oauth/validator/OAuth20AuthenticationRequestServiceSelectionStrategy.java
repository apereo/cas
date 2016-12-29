package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.jasig.cas.client.util.URIBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthCallbackAuthorizeService;

import java.util.Optional;

/**
 * This is {@link OAuth20AuthenticationRequestServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20AuthenticationRequestServiceSelectionStrategy implements AuthenticationRequestServiceSelectionStrategy {
    private static final long serialVersionUID = 8517547235465666978L;
    
    private final ServicesManager servicesManager;
    
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public OAuth20AuthenticationRequestServiceSelectionStrategy(final ServicesManager servicesManager, 
                                                                final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        final URIBuilder builder = new URIBuilder(service.getId());

        final Optional<URIBuilder.BasicNameValuePair> clientId =
                builder.getQueryParams().stream().filter(p -> p.getName().equals(OAuthConstants.CLIENT_ID)).findFirst();

        final Optional<URIBuilder.BasicNameValuePair> redirectUri =
                builder.getQueryParams().stream().filter(p -> p.getName().equals(OAuthConstants.REDIRECT_URI)).findFirst();

        if (clientId.isPresent() && redirectUri.isPresent()) {
            return this.webApplicationServiceFactory.createService(redirectUri.get().getValue());
        }
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        final RegisteredService svc = this.servicesManager.findServiceBy(service);
        return svc instanceof OAuthCallbackAuthorizeService;
    }

    @Override
    public int compareTo(final AuthenticationRequestServiceSelectionStrategy o) {
        return MAX_ORDER - 1;
    }
}
