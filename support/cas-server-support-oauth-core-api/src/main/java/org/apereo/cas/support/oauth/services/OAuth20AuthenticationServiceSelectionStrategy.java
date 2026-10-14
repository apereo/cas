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
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
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

    private static final Set<String> REQUEST_PARAMETER_NAMES =
        Set.of(OAuth20Constants.CLIENT_ID, OAuth20Constants.REDIRECT_URI, OAuth20Constants.GRANT_TYPE);

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
        val parameters = resolveRequestParameters(service);
        val clientId = Optional.ofNullable(parameters.get(OAuth20Constants.CLIENT_ID));

        if (clientId.isPresent()) {
            service.getAttributes().computeIfAbsent(OAuth20Constants.CLIENT_ID,
                _ -> CollectionUtils.wrapList(clientId.get()));

            val redirectUri = Optional.ofNullable(parameters.get(OAuth20Constants.REDIRECT_URI));
            if (redirectUri.isPresent()) {
                return createService(redirectUri.get(), service);
            }
            val grantType = Optional.ofNullable(parameters.get(OAuth20Constants.GRANT_TYPE));
            if (grantType.isPresent()) {
                var id = StringUtils.EMPTY;
                val grantValue = grantType.get();
                if (OAuth20Utils.isGrantType(grantValue, OAuth20GrantTypes.CLIENT_CREDENTIALS)) {
                    LOGGER.debug("Located grant type [{}]; checking for service headers", grantValue);
                    val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
                    val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
                    id = OAuth20Utils.getServiceRequestHeaderIfAny(new JEEContext(request, response));
                }
                if (StringUtils.isBlank(id)) {
                    id = clientId.get();
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

    /*
     * A request object is unpacked and verified once for all the parameters read from it, instead of once per parameter.
     * When the service carries one, its parameters come only from the request object, as before.
     */
    private Map<String, String> resolveRequestParameters(final Service service) {
        return FunctionUtils.doAndHandle(() -> service.getAttributes().containsKey(OAuth20Constants.REQUEST)
                ? resolveJwtRequestParameters(service)
                : resolveQueryParameters(service),
            e -> {
                LoggingUtils.error(LOGGER, e);
                return Map.<String, String>of();
            }).get();
    }

    private Map<String, String> resolveJwtRequestParameters(final Service service) throws Exception {
        val jwtRequest = service.getFirstAttribute(OAuth20Constants.REQUEST, String.class);
        val registeredService = getServicesManager().findServiceBy(service);
        return requestParameterResolver.resolveJwtRequestParameters(jwtRequest, registeredService, REQUEST_PARAMETER_NAMES, String.class);
    }

    private static Map<String, String> resolveQueryParameters(final Service service) throws Exception {
        val parameters = new HashMap<String, String>();
        for (val parameter : new URIBuilder(service.getId()).getQueryParams()) {
            if (REQUEST_PARAMETER_NAMES.contains(parameter.getName()) && parameter.getValue() != null) {
                parameters.putIfAbsent(parameter.getName(), parameter.getValue());
            }
        }
        return parameters;
    }
}
