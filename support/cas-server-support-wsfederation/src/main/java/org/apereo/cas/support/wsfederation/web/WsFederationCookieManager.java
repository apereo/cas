package org.apereo.cas.support.wsfederation.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
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
    private static final String WCTX = "wctx";

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
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        final String wCtx = request.getParameter(WCTX);
        LOGGER.debug("Parameter [{}] received: [{}]", WCTX, wCtx);
        if (StringUtils.isBlank(wCtx)) {
            LOGGER.error("No [{}] parameter is found", WCTX);
            throw new IllegalArgumentException("No " + WCTX + " parameter is found");
        }

        final WsFederationConfiguration configuration = configurations.stream().filter(c -> c.getId().equals(wCtx)).findFirst().orElse(null);
        final CookieRetrievingCookieGenerator cookieGen = configuration.getCookieGenerator();
        final String value = cookieGen.retrieveCookieValue(request);
        if (StringUtils.isBlank(value)) {
            LOGGER.error("No cookie value could be retrieved to determine the state of the delegated authentication session");
            throw new IllegalArgumentException("No cookie could be found to determine session state");
        }
        final String blob = EncodingUtils.hexDecode(value);
        final Map<String, Object> session = serializer.from(blob);
        request.setAttribute(this.themeParamName, session.get(this.themeParamName));
        request.setAttribute(this.localParamName, session.get(this.localParamName));
        request.setAttribute(CasProtocolConstants.PARAMETER_METHOD, session.get(CasProtocolConstants.PARAMETER_METHOD));

        final String serviceKey = CasProtocolConstants.PARAMETER_SERVICE + "-" + wCtx;
        final Service service = (Service) session.get(serviceKey);
        LOGGER.debug("Located service [{}] from session cookie", service);
        context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
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
        session.put(CasProtocolConstants.PARAMETER_SERVICE + "-" + wctx, service);
        final String methods = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methods)) {
            session.put(CasProtocolConstants.PARAMETER_METHOD + "-" + wctx, methods);
        }
        final Object locale = request.getAttribute(this.localParamName);
        if (locale != null) {
            session.put(this.localParamName + "-" + wctx, locale);
        }
        final Object theme = request.getAttribute(this.themeParamName);
        if (theme != null) {
            session.put(this.themeParamName + "-" + wctx, theme);
        }

        final String cookieValue = serializeSessionValues(session);
        final CookieRetrievingCookieGenerator cookieGen = configuration.getCookieGenerator();
        cookieGen.addCookie(request, response, cookieValue);
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        final String blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
