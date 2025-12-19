package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.jooq.lambda.Unchecked;
import org.pac4j.jee.context.JEEContext;

/**
 * This is {@link OAuth20AuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class OAuth20AuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    @Serial
    private static final long serialVersionUID = 8517547235465666978L;

    private final String callbackUrl;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    public OAuth20AuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
                                                         final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                         final String callbackUrl,
                                                         final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceFactory);
        this.callbackUrl = callbackUrl;
        this.requestParameterResolver = requestParameterResolver;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val clientId = resolveClientIdFromService(service);

        if (clientId.isPresent()) {
            service.getAttributes().computeIfAbsent(OAuth20Constants.CLIENT_ID,
                _ -> CollectionUtils.wrapList(clientId.get().getValue()));

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
                    val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
                    id = OAuth20Utils.getServiceRequestHeaderIfAny(new JEEContext(request, response));
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

    private Optional<NameValuePair> resolveRedirectUri(final Service service) {
        return getRequestParameter(service, OAuth20Constants.REDIRECT_URI);
    }

    private Optional<NameValuePair> resolveGrantType(final Service service) {
        return getRequestParameter(service, OAuth20Constants.GRANT_TYPE);
    }

    private Optional<NameValuePair> resolveClientIdFromService(final Service service) {
        return getRequestParameter(service, OAuth20Constants.CLIENT_ID);
    }

    private Optional<NameValuePair> getRequestParameter(final Service service, final String name) {
        try {
            val value = getJwtRequestParameter(service, name)
                .or(Unchecked.supplier(() -> {
                    val builder = new URIBuilder(service.getId());
                    return builder.getQueryParams()
                        .stream()
                        .filter(p -> p.getName().equals(name))
                        .map(NameValuePair::getValue)
                        .findFirst();
                }));
            return value.map(v -> new BasicNameValuePair(name, v));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    private Optional<String> getJwtRequestParameter(final Service service,
                                                    final String paramName) throws Exception {
        if (service.getAttributes().containsKey(OAuth20Constants.REQUEST)) {
            val jwtRequest = service.getFirstAttribute(OAuth20Constants.REQUEST, String.class);
            val registeredService = getServicesManager().findServiceBy(service);
            val paramValue = requestParameterResolver.resolveJwtRequestParameter(jwtRequest, registeredService, paramName, String.class);
            return Optional.of(paramValue);
        }
        return Optional.empty();
    }
}
