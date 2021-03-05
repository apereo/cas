package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.util.Optional;

/**
 * This is {@link OAuth20AuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class OAuth20AuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = 8517547235465666978L;

    private final String callbackUrl;

    public OAuth20AuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        final String callbackUrl) {
        super(servicesManager, webApplicationServiceFactory);
        this.callbackUrl = callbackUrl;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val clientId = resolveClientIdFromService(service);

        if (clientId.isPresent()) {
            val redirectUri = resolveRedirectUri(service);
            if (redirectUri.isPresent()) {
                return createService(redirectUri.get().getValue(), service);
            }
            val grantType = resolveGrantType(service);
            if (grantType.isPresent()) {
                var id = StringUtils.EMPTY;
                val grantValue = grantType.get().getValue();
                if (OAuth20Utils.isGrantType(grantValue, OAuth20GrantTypes.CLIENT_CREDENTIALS)) {
                    LOGGER.debug("Located grant type [{}]; checking for service headers", grantValue);
                    val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                    id = OAuth20Utils.getServiceRequestHeaderIfAny(request);
                }
                if (StringUtils.isBlank(id)) {
                    id = clientId.get().getValue();
                }
                LOGGER.debug("Built web application service based on identifier [{}]", id);
                return createService(id, service);
            }
        }
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        val svc = getServicesManager().findServiceBy(service);
        val res = svc != null && service.getId().startsWith(this.callbackUrl);
        val msg = String.format("Authentication request is%s identified as an OAuth request",
            BooleanUtils.toString(res, StringUtils.EMPTY, " not"));
        LOGGER.trace(msg);
        return res;
    }

    private static Optional<NameValuePair> resolveClientIdFromService(final Service service) {
        try {
            val builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(OAuth20Constants.CLIENT_ID))
                .findFirst();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    @SneakyThrows
    private static Optional<NameValuePair> resolveRedirectUri(final Service service) {
        val builder = new URIBuilder(service.getId());
        return builder.getQueryParams()
            .stream()
            .filter(p -> p.getName().equals(OAuth20Constants.REDIRECT_URI))
            .findFirst();
    }

    @SneakyThrows
    private static Optional<NameValuePair> resolveGrantType(final Service service) {
        val builder = new URIBuilder(service.getId());
        return builder.getQueryParams()
            .stream()
            .filter(p -> p.getName().equals(OAuth20Constants.GRANT_TYPE))
            .findFirst();
    }
}
