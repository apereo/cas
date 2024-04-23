package org.apereo.cas.web.flow.controller;

import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultDelegatedAuthenticationNavigationController}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Controller("defaultDelegatedAuthenticationNavigationController")
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultDelegatedAuthenticationNavigationController {
    /**
     * Endpoint path controlled by this controller that receives the response to PAC4J.
     */
    protected static final String ENDPOINT_RESPONSE = "login/{clientName}";

    private final DelegatedClientAuthenticationConfigurationContext configurationContext;

    /**
     * Redirect response to flow. Receives the CAS, OAuth, OIDC, etc. callback response, adjust it to work with
     * the login webflow, and redirects the requests to the login webflow endpoint.
     *
     * @param clientName the path-based parameter that provider the pac4j client name
     * @param request    the request
     * @param response   the response
     * @return the view
     * @throws Exception the exception
     */
    @GetMapping(ENDPOINT_RESPONSE)
    public View redirectResponseToFlow(
        @PathVariable("clientName")
        final String clientName,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        return buildRedirectViewBackToFlow(clientName, request);
    }

    /**
     * Redirect response to flow. Receives the CAS, OAuth, OIDC, etc. callback response, adjust it to work with
     * the login webflow, and redirects the requests to the login webflow endpoint.
     *
     * @param clientName the path-based parameter that provider the pac4j client name
     * @param request    the request
     * @param response   the response
     * @return the view
     * @throws Exception the exception
     */
    @PostMapping(ENDPOINT_RESPONSE)
    public View postResponseToFlow(
        @PathVariable("clientName")
        final String clientName,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        return buildRedirectViewBackToFlow(clientName, request);
    }

    /**
     * Build redirect view back to flow view.
     *
     * @param clientName the client name
     * @param request    the request
     * @return the view
     * @throws Exception the exception
     */
    protected View buildRedirectViewBackToFlow(final String clientName, final HttpServletRequest request) throws Exception {
        val urlBuilder = new URIBuilder(configurationContext.getCasProperties().getServer().getLoginUrl());
        request.getParameterMap().forEach((k, v) -> {
            val value = request.getParameter(k);
            urlBuilder.addParameter(k, value);
        });
        urlBuilder.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, clientName);
        val url = urlBuilder.toString();
        LOGGER.debug("Received response from client [{}]; Redirecting to [{}]", clientName, url);
        return new RedirectView(url);
    }

}
