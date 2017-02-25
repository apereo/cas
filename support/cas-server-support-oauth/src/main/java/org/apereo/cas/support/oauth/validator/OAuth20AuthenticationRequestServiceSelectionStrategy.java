package org.apereo.cas.support.oauth.validator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is {@link OAuth20AuthenticationRequestServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20AuthenticationRequestServiceSelectionStrategy implements AuthenticationRequestServiceSelectionStrategy {
    private static final long serialVersionUID = 8517547235465666978L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthenticationRequestServiceSelectionStrategy.class);

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final String callbackUrl;

    public OAuth20AuthenticationRequestServiceSelectionStrategy(final ServicesManager servicesManager,
                                                                final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                                final String callbackUrl) {
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        final Optional<NameValuePair> clientId = resolveClientIdFromService(service);
        final Optional<NameValuePair> redirectUri = resolveRedirectUri(service);

        if (clientId.isPresent() && redirectUri.isPresent()) {
            return this.webApplicationServiceFactory.createService(redirectUri.get().getValue());
        }
        return service;
    }

    private Optional<NameValuePair> resolveClientIdFromService(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            return builder.getQueryParams().stream().filter(p -> p.getName().equals(OAuthConstants.CLIENT_ID)).findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<NameValuePair> resolveRedirectUri(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            return builder.getQueryParams().stream().filter(p -> p.getName().equals(OAuthConstants.REDIRECT_URI)).findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(final Service service) {
        final RegisteredService svc = this.servicesManager.findServiceBy(service);
        final boolean res = svc != null && service.getId().startsWith(this.callbackUrl);

        LOGGER.debug("Authentication request is{}identified as an OAuth request",
                BooleanUtils.toString(res, StringUtils.EMPTY, " not "));
        return res;
    }

    @Override
    public int compareTo(final AuthenticationRequestServiceSelectionStrategy o) {
        return 0;
    }
}
