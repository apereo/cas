package org.apereo.cas.web;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DelegatedClientNavigationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller("delegatedClientNavigationController")
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class DelegatedClientNavigationController {

    /**
     * Endpoint path controlled by this controller that receives the response to PAC4J.
     */
    private static final String ENDPOINT_RESPONSE = "login/{clientName}";

    private final Clients clients;

    private final DelegatedClientWebflowManager delegatedClientWebflowManager;

    private final SessionStore<JEEContext> sessionStore;

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
            val clientResult = this.clients.findClient(clientName);
            if (clientResult.isEmpty()) {
                throw new UnauthorizedServiceException("Unable to locate client " + clientName);
            }
            val client = IndirectClient.class.cast(clientResult.get());
            client.init();
            val webContext = new JEEContext(request, response, this.sessionStore);
            val ticket = delegatedClientWebflowManager.store(webContext, client);

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
    @RequestMapping(value = ENDPOINT_RESPONSE, method = {RequestMethod.GET, RequestMethod.POST})
    public View redirectResponseToFlow(@PathVariable("clientName") final String clientName,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) {
        return buildRedirectViewBackToFlow(clientName, request);
    }

    /**
     * Build redirect view back to flow view.
     *
     * @param clientName the client name
     * @param request    the request
     * @return the view
     */
    @SneakyThrows
    protected View buildRedirectViewBackToFlow(final String clientName, final HttpServletRequest request) {

        val urlBuilder = new URIBuilder(String.valueOf(request.getRequestURL()));
        request.getParameterMap().forEach((k, v) -> {
            val value = request.getParameter(k);
            urlBuilder.addParameter(k, value);
        });

        urlBuilder.setPath(urlBuilder.getPath().replace('/' + clientName, StringUtils.EMPTY));
        urlBuilder.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, clientName);

        val url = urlBuilder.toString();
        LOGGER.debug("Received a response for client [{}], redirecting the login flow [{}]", clientName, url);
        return new RedirectView(url);
    }

    /**
     * Gets resulting view.
     *
     * @param client     the client
     * @param webContext the web context
     * @param ticket     the ticket
     * @return the resulting view
     */
    @SneakyThrows
    protected View getResultingView(final IndirectClient<Credentials> client, final JEEContext webContext, final Ticket ticket) {
        client.init();
        val actionResult = client.getRedirectionActionBuilder().redirect(webContext);
        if (actionResult.isPresent()) {
            val action = actionResult.get();
            LOGGER.debug("Determined final redirect action for client [{}] as [{}]", client, action);

            if (action instanceof WithLocationAction) {
                val foundAction = WithLocationAction.class.cast(action);
                val builder = new URIBuilder(foundAction.getLocation());
                val url = builder.toString();
                LOGGER.debug("Redirecting client [{}] to [{}] based on identifier [{}]", client.getName(), url, ticket.getId());
                return new RedirectView(url);
            }
            if (action instanceof WithContentAction) {
                val seeOtherAction = WithContentAction.class.cast(action);
                return new DynamicHtmlView(seeOtherAction.getContent());
            }
        }
        LOGGER.warn("Unable to determine redirect action for client [{}]", client);
        return null;
    }
}
