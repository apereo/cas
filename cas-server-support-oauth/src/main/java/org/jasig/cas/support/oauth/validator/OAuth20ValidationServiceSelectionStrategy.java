package org.jasig.cas.support.oauth.validator;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.client.util.URIBuilder;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.validation.ValidationServiceSelectionStrategy;
import org.jasig.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link OAuth20ValidationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("oauth20ValidationServiceSelectionStrategy")
public class OAuth20ValidationServiceSelectionStrategy implements ValidationServiceSelectionStrategy {
    private static final long serialVersionUID = 8517547235465666978L;

    @Value("${server.prefix:http://localhost:8080/cas}")
    private String casServerUrl;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

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
        return svc != null && svc instanceof OAuthCallbackAuthorizeService;
    }

    @Override
    public int compareTo(final ValidationServiceSelectionStrategy o) {
        return MAX_ORDER - 1;
    }
}
