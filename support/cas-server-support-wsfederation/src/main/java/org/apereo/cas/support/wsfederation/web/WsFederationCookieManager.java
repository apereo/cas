package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
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
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val wCtx = request.getParameter(WCTX);
        LOGGER.debug("Parameter [{}] received: [{}]", WCTX, wCtx);
        if (StringUtils.isBlank(wCtx)) {
            LOGGER.error("No [{}] parameter is found", WCTX);
            throw new IllegalArgumentException("No " + WCTX + " parameter is found");
        }

        val configuration = configurations.stream()
            .filter(c -> c.getId().equalsIgnoreCase(wCtx))
            .findFirst()
            .orElseThrow();
        
        val cookieGen = configuration.getCookieGenerator();
        val value = cookieGen.retrieveCookieValue(request);
        if (StringUtils.isBlank(value)) {
            LOGGER.error("No cookie value could be retrieved to determine the state of the delegated authentication session");
            throw new IllegalArgumentException("No cookie could be found to determine session state");
        }
        val blob = EncodingUtils.hexDecode(value);
        val session = serializer.from(blob);
        request.setAttribute(this.themeParamName, session.get(this.themeParamName));
        request.setAttribute(this.localParamName, session.get(this.localParamName));
        request.setAttribute(CasProtocolConstants.PARAMETER_METHOD, session.get(CasProtocolConstants.PARAMETER_METHOD));

        val serviceKey = CasProtocolConstants.PARAMETER_SERVICE + '-' + wCtx;
        val service = (Service) session.get(serviceKey);
        LOGGER.debug("Located service [{}] from session cookie", service);
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
    public void store(final HttpServletRequest request, final HttpServletResponse response,
                      final String wctx, final Service service, final WsFederationConfiguration configuration) {
        val session = new HashMap<String, Object>();
        session.put(CasProtocolConstants.PARAMETER_SERVICE + '-' + wctx, service);
        val methods = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methods)) {
            session.put(CasProtocolConstants.PARAMETER_METHOD + '-' + wctx, methods);
        }
        val locale = request.getAttribute(this.localParamName);
        if (locale != null) {
            session.put(this.localParamName + '-' + wctx, locale);
        }
        val theme = request.getAttribute(this.themeParamName);
        if (theme != null) {
            session.put(this.themeParamName + '-' + wctx, theme);
        }

        val cookieValue = serializeSessionValues(session);
        val cookieGen = configuration.getCookieGenerator();
        cookieGen.addCookie(request, response, cookieValue);
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        val blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
