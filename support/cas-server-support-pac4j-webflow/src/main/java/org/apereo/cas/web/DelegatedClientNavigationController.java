package org.apereo.cas.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.Pac4jUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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
     * Endpoint path controlled by this controller to make the redirect.
     */
    public static final String ENDPOINT_REDIRECT = "/clientredirect";

    private final Clients clients;
    private final DelegatedClientWebflowManager delegatedClientWebflowManager;

    /**
     * Redirect to provider view.
     *
     * @param request  the request
     * @param response the response
     * @return the view
     */
    @GetMapping(ENDPOINT_REDIRECT)
    public View redirectToProvider(final HttpServletRequest request, final HttpServletResponse response) {
        final String clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        try {
            final IndirectClient<Credentials, CommonProfile> client = (IndirectClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
            final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
            final Ticket ticket = delegatedClientWebflowManager.store(webContext);

            webContext.getSessionStore().set(webContext, SAML2Client.SAML_RELAY_STATE_ATTRIBUTE, ticket.getId());
            final RedirectAction action = client.getRedirectAction(webContext);
            if (RedirectAction.RedirectType.SUCCESS.equals(action.getType())) {
                return new DynamicHtmlView(action.getContent());
            }

            final URIBuilder builder = new URIBuilder(action.getLocation());
            builder.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            final String url = builder.toString();
            LOGGER.debug("Redirecting client [{}] to [{}] based on identifier [{}]", client.getName(), url, ticket.getId());

            return new RedirectView(url);
        } catch (final HttpAction e) {
            if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
                LOGGER.debug("Authentication request was denied from the provider [{}]", clientName);
            } else {
                LOGGER.warn(e.getMessage(), e);
            }
            throw new UnauthorizedServiceException(e.getMessage(), e);
        }
    }

    @RequiredArgsConstructor
    private static class DynamicHtmlView implements View {
        private final String html;

        public String getContentType() {
            return MediaType.TEXT_HTML_VALUE;
        }

        public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
            response.setContentType(this.getContentType());
            FileCopyUtils.copy(this.html, response.getWriter());
        }
    }
}
