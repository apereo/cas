package org.apereo.cas.web;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseDelegatedAuthenticationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseDelegatedAuthenticationController {

    /**
     * Endpoint path controlled by this controller that receives the response to PAC4J.
     */
    protected static final String ENDPOINT_RESPONSE = "login/{clientName}";

    private final Clients clients;

    private final DelegatedClientWebflowManager delegatedClientWebflowManager;

    private final SessionStore<JEEContext> sessionStore;

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
        val actionResult = client.getRedirectionActionBuilder().getRedirectionAction(webContext);
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
