package org.apereo.cas.support.pac4j.web.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an action to put at the beginning of the webflow.
 * <p>
 * Before any authentication, redirection urls are computed for the different clients defined as well as the theme,
 * locale, method and service are saved into the web session.</p>
 * After authentication, appropriate information are expected on this callback url to finish the authentication
 * process with the provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DelegatedClientAuthenticationAction extends AbstractAction {

    /**
     * Stop the webflow for pac4j and route to view.
     */
    public static final String STOP_WEBFLOW = "stopWebflow";

    /**
     * Stop the webflow.
     */
    public static final String STOP = "stop";

    /**
     * Client action state id in the webflow.
     */
    public static final String CLIENT_ACTION = "clientAction";

    /**
     * All the urls and names of the pac4j clients.
     */
    public static final String PAC4J_URLS = "pac4jUrls";

    /**
     * View id that stops the webflow.
     */
    public static final String VIEW_ID_STOP_WEBFLOW = "casPac4jStopWebflow";

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");

    private static final Pattern PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN = Pattern.compile("\\W");

    private final Clients clients;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final CentralAuthenticationService centralAuthenticationService;

    private final String themeParamName;

    private final String localParamName;

    private final boolean autoRedirect;

    private final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        LOGGER.debug("Delegated authentication is handled by client name [{}]", clientName);
        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            return stopWebflow();
        }
        if (StringUtils.isNotBlank(clientName)) {
            final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
            LOGGER.debug("Delegated authentication client is [{}]", client);
            final Service service = (Service) session.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            LOGGER.debug("Retrieve service: [{}]", service);
            if (service != null) {
                request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
                if (!isDelegatedClientAuthorizedForService(client, service)) {
                    LOGGER.warn("Delegated client [{}] is not authorized by service [{}]", client, service);
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
                }
            }
            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
            } catch (final Exception e) {
                LOGGER.debug("The request requires http action.", e);
                return stopWebflow();
            }
            restoreRequestAttribute(request, session, this.themeParamName);
            restoreRequestAttribute(request, session, this.localParamName);
            restoreRequestAttribute(request, session, CasProtocolConstants.PARAMETER_METHOD);
            if (credentials != null) {
                final ClientCredential clientCredential = new ClientCredential(credentials);
                final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, clientCredential);
                final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
                WebUtils.putTicketGrantingTicketInScopes(context, tgt);
                return success();
            }
        }
        // no or aborted authentication : go to login page
        prepareForLoginPage(context);
        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            return stopWebflow();
        }
        if (this.autoRedirect) {
            final Set<ProviderLoginPageConfiguration> urls = context.getFlowScope().get(PAC4J_URLS, Set.class);
            if (urls != null && urls.size() == 1) {
                final ProviderLoginPageConfiguration cfg = urls.stream().findFirst().get();
                LOGGER.debug("Auto-redirecting to client url [{}]", cfg.getRedirectUrl());
                response.sendRedirect(cfg.getRedirectUrl());
                final ExternalContext externalContext = context.getExternalContext();
                externalContext.recordResponseComplete();
                return stopWebflow();
            }
        }
        return error();
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     */
    protected void prepareForLoginPage(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
        final Service service = WebUtils.getService(context);
        LOGGER.debug("Save service: [{}]", service);
        session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        saveRequestParameter(request, session, this.themeParamName);
        saveRequestParameter(request, session, this.localParamName);
        saveRequestParameter(request, session, CasProtocolConstants.PARAMETER_METHOD);
        final Set<ProviderLoginPageConfiguration> urls = new LinkedHashSet<>();
        this.clients.findAllClients().stream().filter(client -> client instanceof IndirectClient
            && isDelegatedClientAuthorizedForService(client, service))
            .forEach(client -> {
                try {
                    final IndirectClient indirectClient = (IndirectClient) client;
                    final String name = client.getName();
                    final Matcher matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
                    final String type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();
                    final String redirectionUrl = indirectClient.getRedirectAction(webContext).getLocation();
                    LOGGER.debug("[{}] -> [{}]", name, redirectionUrl);
                    urls.add(new ProviderLoginPageConfiguration(name, redirectionUrl, type, getCssClass(name)));
                } catch (final HttpAction e) {
                    if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
                        LOGGER.debug("Authentication request was denied from the provider [{}]", client.getName());
                    } else {
                        LOGGER.warn(e.getMessage(), e);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Cannot process client [{}]", client, e);
                }
            });
        if (!urls.isEmpty()) {
            context.getFlowScope().put(PAC4J_URLS, urls);
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no clients are configured, or the current access strategy rules prohibit CAS from using authentication providers for this request.");
        }
    }

    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param name Name of the provider
     */
    private String getCssClass(final String name) {
        String computedCssClass = "fa fa-lock";
        if (name != null) {
            computedCssClass = computedCssClass.concat(" " + PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN.matcher(name).replaceAll("-"));
        }
        LOGGER.debug("cssClass for [{}] is [{}]", name, computedCssClass);
        return computedCssClass;
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name    The name of the parameter
     */
    private static void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session, final String name) {
        final String value = (String) session.getAttribute(name);
        request.setAttribute(name, value);
    }

    /**
     * Save a request parameter in the web session.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name    The name of the parameter
     */
    private static void saveRequestParameter(final HttpServletRequest request, final HttpSession session, final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }

    private Event stopWebflow() {
        return new Event(this, STOP);
    }

    /**
     * Determine if request has errors.
     *
     * @param request the request
     * @param status  the status
     * @return the optional model and view, if request is an error.
     */
    public static Optional<ModelAndView> hasDelegationRequestFailed(final HttpServletRequest request, final int status) {
        final Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey("error") || params.containsKey("error_code") || params.containsKey("error_description") || params.containsKey("error_message")) {
            final Map<String, Object> model = new HashMap<>();
            if (params.containsKey("error_code")) {
                model.put("code", StringEscapeUtils.escapeHtml4(request.getParameter("error_code")));
            } else {
                model.put("code", status);
            }
            model.put("error", StringEscapeUtils.escapeHtml4(request.getParameter("error")));
            model.put("reason", StringEscapeUtils.escapeHtml4(request.getParameter("error_reason")));
            if (params.containsKey("error_description")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_description")));
            } else if (params.containsKey("error_message")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_message")));
            }
            model.put(CasProtocolConstants.PARAMETER_SERVICE, request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
            model.put("client", StringEscapeUtils.escapeHtml4(request.getParameter("client_name")));
            LOGGER.debug("Delegation request has failed. Details are [{}]", model);
            return Optional.of(new ModelAndView("casPac4jStopWebflow", model));
        }
        return Optional.empty();
    }

    private boolean isDelegatedClientAuthorizedForService(final Client client, final Service service) {
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service access for [{}] is denied", registeredService);
                return false;
            }
            LOGGER.debug("Located registered service definition [{}] matching [{}]", registeredService, service);
            final RegisteredServiceDelegatedAuthenticationPolicy policy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
            if (policy != null) {
                LOGGER.debug("Evaluating delegated authentication policy [{}] for client [{}] and service [{}]", policy, client, registeredService);
                if (policy.isProviderAllowed(client.getName(), registeredService)) {
                    LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, client);
                    return true;
                }
                LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), client);
                return false;
            }
        }
        return true;
    }

    /**
     * The Provider login page configuration.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class ProviderLoginPageConfiguration implements Serializable {

        private static final long serialVersionUID = 6216882278086699364L;

        private final String name;

        private final String redirectUrl;

        private final String type;

        private final String cssClass;
    }
}
