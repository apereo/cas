package org.apereo.cas.support.wsfederation.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private final String themeParamName;
    private final String localParamName;

    private final WsFederationCookieSerializer serializer = new WsFederationCookieSerializer();

    /**
     * Retrieve service.
     *
     * @param context the request context
     * @return the service
     */
    public Service retrieve(final RequestContext context) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        final var wCtx = request.getParameter(WCTX);
        LOGGER.debug("Parameter [{}] received: [{}]", WCTX, wCtx);
        if (StringUtils.isBlank(wCtx)) {
            LOGGER.error("No [{}] parameter is found", WCTX);
            throw new IllegalArgumentException("No " + WCTX + " parameter is found");
        }

        final var configuration = configurations.stream()
            .filter(c -> c.getId().equalsIgnoreCase(wCtx))
            .findFirst()
            .orElse(null);
        final var cookieGen = configuration.getCookieGenerator();
        final var value = cookieGen.retrieveCookieValue(request);
        if (StringUtils.isBlank(value)) {
            LOGGER.error("No cookie value could be retrieved to determine the state of the delegated authentication session");
            throw new IllegalArgumentException("No cookie could be found to determine session state");
        }
        final var blob = EncodingUtils.hexDecode(value);
        final Map<String, Object> session = serializer.from(blob);
        request.setAttribute(this.themeParamName, session.get(this.themeParamName));
        request.setAttribute(this.localParamName, session.get(this.localParamName));
        request.setAttribute(CasProtocolConstants.PARAMETER_METHOD, session.get(CasProtocolConstants.PARAMETER_METHOD));

        final var serviceKey = CasProtocolConstants.PARAMETER_SERVICE + '-' + wCtx;
        final var service = (Service) session.get(serviceKey);
        LOGGER.debug("Located service [{}] from session cookie", service);
        WebUtils.putService(context, service);
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
    public void store(final HttpServletRequest request, final HttpServletResponse response,
                      final String wctx, final Service service, final WsFederationConfiguration configuration) {
        final Map<String, Object> session = new LinkedHashMap<>();
        session.put(CasProtocolConstants.PARAMETER_SERVICE + '-' + wctx, service);
        final var methods = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methods)) {
            session.put(CasProtocolConstants.PARAMETER_METHOD + '-' + wctx, methods);
        }
        final var locale = request.getAttribute(this.localParamName);
        if (locale != null) {
            session.put(this.localParamName + '-' + wctx, locale);
        }
        final var theme = request.getAttribute(this.themeParamName);
        if (theme != null) {
            session.put(this.themeParamName + '-' + wctx, theme);
        }

        final var cookieValue = serializeSessionValues(session);
        final var cookieGen = configuration.getCookieGenerator();
        cookieGen.addCookie(request, response, cookieValue);
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        final var blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
