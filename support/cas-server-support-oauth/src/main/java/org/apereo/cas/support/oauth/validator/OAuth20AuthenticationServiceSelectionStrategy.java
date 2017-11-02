package org.apereo.cas.support.oauth.validator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link OAuth20AuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20AuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = 8517547235465666978L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthenticationServiceSelectionStrategy.class);

    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    private final String callbackUrl;
    private final int order = Ordered.HIGHEST_PRECEDENCE;

    public OAuth20AuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
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
        final Optional<NameValuePair> grantType = resolveGrantType(service);

        if (clientId.isPresent()) {
            if (redirectUri.isPresent()) {
                return this.webApplicationServiceFactory.createService(redirectUri.get().getValue());
            }
            if (grantType.isPresent()) {
                String id = null;
                final String grantValue = grantType.get().getValue();
                if (OAuth20Utils.isGrantType(grantValue, OAuth20GrantTypes.CLIENT_CREDENTIALS)) {
                    LOGGER.debug("Located grant type [{}]; checking for service headers");
                    final HttpServletRequest request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                    id = OAuth20Utils.getServiceRequestHeaderIfAny(request);
                }
                if (StringUtils.isBlank(id)) {
                    id = clientId.get().getValue();
                }
                LOGGER.debug("Built web application service based on identifier [{}]", id);
                return this.webApplicationServiceFactory.createService(id);
            }
        }
        return service;
    }

    private static Optional<NameValuePair> resolveClientIdFromService(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName()
                    .equals(OAuth20Constants.CLIENT_ID))
                    .findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

    private static Optional<NameValuePair> resolveRedirectUri(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(OAuth20Constants.REDIRECT_URI))
                    .findFirst();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

    private static Optional<NameValuePair> resolveGrantType(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName()
                    .equals(OAuth20Constants.GRANT_TYPE))
                    .findFirst();
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
    public int getOrder() {
        return this.order;
    }
}
