package org.apereo.cas.support.wsfederation.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link WsFederationCookieManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WsFederationCookieManager {
    /**
     * ws-fed {@code wctx} parameter.
     */
    public static final String WCTX = "wctx";

    private final Collection<WsFederationConfiguration> configurations;
    private final CasConfigurationProperties casProperties;

    private final WsFederationServerStateSerializer serializer;

    /**
     * Retrieve service.
     *
     * @param context the request context
     * @return the service
     */
    public Service retrieve(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val contextId = request.getParameter(WCTX);
        LOGGER.debug("Parameter [{}] received: [{}]", WCTX, contextId);
        if (StringUtils.isBlank(contextId)) {
            LOGGER.error("No [{}] parameter is found", WCTX);
            throw new IllegalArgumentException("No " + WCTX + " parameter is found");
        }

        val configuration = configurations.stream()
            .filter(cookie -> cookie.getId().equalsIgnoreCase(contextId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Could not locate WsFederation configuration for " + contextId));

        val cookieGen = configuration.getCookieGenerator();
        var serverState = cookieGen.retrieveCookieValue(request);
        if (StringUtils.isBlank(serverState)) {
            serverState = Optional.ofNullable(request.getSession(false))
                .map(session -> session.getAttribute(configuration.getId()))
                .map(String.class::cast)
                .orElse(null);
        }
        if (StringUtils.isBlank(serverState)) {
            LOGGER.error("No server state value could be retrieved to determine the state of the delegated authentication session");
            throw new IllegalArgumentException("No state could be found to determine session state");
        }
        val blob = EncodingUtils.hexDecode(serverState);
        val session = serializer.from(blob);
        request.setAttribute(casProperties.getTheme().getParamName(), session.get(casProperties.getTheme().getParamName()));
        request.setAttribute(casProperties.getLocale().getParamName(), session.get(casProperties.getLocale().getParamName()));
        request.setAttribute(CasProtocolConstants.PARAMETER_METHOD, session.get(CasProtocolConstants.PARAMETER_METHOD));

        val serviceKey = CasProtocolConstants.PARAMETER_SERVICE + '-' + contextId;
        val service = (Service) session.get(serviceKey);
        LOGGER.debug("Located service [{}] from session", service);
        WebUtils.putServiceIntoFlowScope(context, service);
        return service;
    }

    /**
     * Store.
     *
     * @param request       the request
     * @param response      the response
     * @param wctx          the wctx
     * @param service       the service
     * @param configuration the configuration
     */
    public void store(final HttpServletRequest request,
                      final HttpServletResponse response,
                      final String wctx,
                      final Service service,
                      final WsFederationConfiguration configuration) {
        val details = new HashMap<String, Object>();

        details.put(CasProtocolConstants.PARAMETER_SERVICE + '-' + wctx, service);
        val methods = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methods)) {
            details.put(CasProtocolConstants.PARAMETER_METHOD + '-' + wctx, methods);
        }
        val locale = request.getAttribute(casProperties.getLocale().getParamName());
        if (locale != null) {
            details.put(casProperties.getLocale().getParamName() + '-' + wctx, locale);
        }
        val theme = request.getAttribute(casProperties.getTheme().getParamName());
        if (theme != null) {
            details.put(casProperties.getTheme().getParamName() + '-' + wctx, theme);
        }

        val cookieValue = serializeSessionValues(details);
        val cookieGen = configuration.getCookieGenerator();
        LOGGER.debug("Adding WsFederation cookie [{}] with value [{}]", cookieGen.getCookieName(), cookieValue);
        cookieGen.addCookie(request, response, cookieValue);
        Optional.ofNullable(request.getSession(false))
            .ifPresent(session -> session.setAttribute(configuration.getId(), cookieValue));
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        val blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
