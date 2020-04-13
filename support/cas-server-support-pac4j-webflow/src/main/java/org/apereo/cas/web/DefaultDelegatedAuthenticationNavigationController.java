package org.apereo.cas.web;

import org.apereo.cas.services.UnauthorizedServiceException;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultDelegatedAuthenticationNavigationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultDelegatedAuthenticationNavigationController extends BaseDelegatedAuthenticationController {

    public DefaultDelegatedAuthenticationNavigationController(final Clients clients,
                                                              final DelegatedClientWebflowManager delegatedClientWebflowManager,
                                                              final SessionStore<JEEContext> sessionStore) {
        super(clients, delegatedClientWebflowManager, sessionStore);
    }

    /**
     * Redirect to provider. Receive the client name from the request and then try to determine and build the endpoint url
     * for the redirection. The redirection data/url must contain a delegated client ticket id so that the request be can
     * restored on the trip back. SAML clients use the relay-state session attribute while others use request parameters.
     *
     * @param request  the request
     * @param response the response
     * @return the view
     */
    @GetMapping(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT)
    public View redirectToProvider(final HttpServletRequest request, final HttpServletResponse response) {
        var clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        if (StringUtils.isBlank(clientName)) {
            clientName = (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        }

        try {
            if (StringUtils.isBlank(clientName)) {
                throw new UnauthorizedServiceException("No client name parameter is provided in the incoming request");
            }
            val clientResult = getClients().findClient(clientName);
            if (clientResult.isEmpty()) {
                throw new UnauthorizedServiceException("Unable to locate client " + clientName);
            }
            val client = IndirectClient.class.cast(clientResult.get());
            client.init();
            val webContext = new JEEContext(request, response, getSessionStore());
            val ticket = getDelegatedClientWebflowManager().store(webContext, client);

            return getResultingView(client, webContext, ticket);
        } catch (final HttpAction e) {
            if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
                LOGGER.debug("Authentication request was denied from the provider [{}]", clientName, e);
            } else {
                LOGGER.warn(e.getMessage(), e);
            }
            throw new UnauthorizedServiceException(e.getMessage(), e);
        }
    }

    /**
     * Redirect response to flow. Receives the CAS, OAuth, OIDC, etc. callback response, adjust it to work with
     * the login webflow, and redirects the requests to the login webflow endpoint.
     *
     * @param clientName the path-based parameter that provider the pac4j client name
     * @param request    the request
     * @param response   the response
     * @return the view
     */
    @GetMapping(value = ENDPOINT_RESPONSE)
    public View redirectResponseToFlow(@PathVariable("clientName") final String clientName,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) {
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
     */
    @PostMapping(value = ENDPOINT_RESPONSE)
    public View postResponseToFlow(@PathVariable("clientName") final String clientName,
                                   final HttpServletRequest request,
                                   final HttpServletResponse response) {
        return buildRedirectViewBackToFlow(clientName, request);
    }
}
