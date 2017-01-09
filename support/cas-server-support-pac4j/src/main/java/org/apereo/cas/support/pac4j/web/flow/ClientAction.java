package org.apereo.cas.support.pac4j.web.flow;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClientAction extends AbstractAction {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAction.class);

    private Clients clients;

    private AuthenticationSystemSupport authenticationSystemSupport;

    private CentralAuthenticationService centralAuthenticationService;

    private final CasConfigurationProperties casProperties;

    /**
     * Build the ClientAction.
     *
     * @param casProperties the cas properties
     */
    public ClientAction(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // get client
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        LOGGER.debug("clientName: {}", clientName);

        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            return stopWebflow();
        }
        // it's an authentication
        if (StringUtils.isNotBlank(clientName)) {
            // get client
            final BaseClient<Credentials, CommonProfile> client =
                    (BaseClient<Credentials, CommonProfile>) this.clients
                            .findClient(clientName);
            LOGGER.debug("Client: {}", client);

            // get credentials
            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                LOGGER.debug("Retrieved credentials: {}", credentials);
            } catch (final Exception e) {
                LOGGER.debug("The request requires http action", e);
                return stopWebflow();
            }

            // retrieve parameters from web session
            final Service service = (Service) session.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            LOGGER.debug("Retrieve service: {}", service);
            if (service != null) {
                request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            restoreRequestAttribute(request, session, casProperties.getTheme().getParamName());
            restoreRequestAttribute(request, session, casProperties.getLocale().getParamName());
            restoreRequestAttribute(request, session, CasProtocolConstants.PARAMETER_METHOD);

            // credentials not null -> try to authenticate
            if (credentials != null) {
                final AuthenticationResult authenticationResult =
                        this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service,
                                new ClientCredential(credentials));

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
        return error();
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     * @throws HttpAction the http action
     */
    protected void prepareForLoginPage(final RequestContext context) throws HttpAction {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // save parameters in web session
        final WebApplicationService service = WebUtils.getService(context);
        LOGGER.debug("save service: {}", service);
        session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        saveRequestParameter(request, session, casProperties.getTheme().getParamName());
        saveRequestParameter(request, session, casProperties.getLocale().getParamName());
        saveRequestParameter(request, session, CasProtocolConstants.PARAMETER_METHOD);

        final Set<ProviderLoginPageConfiguration> urls = new LinkedHashSet<>();
        // for all clients, generate redirection urls
        for (final Client client : this.clients.findAllClients()) {
            try {
                final IndirectClient indirectClient = (IndirectClient) client;
                // clean Client suffix for default names
                final String name = StringUtils.remove(client.getName(), "Client");
                final String redirectionUrl = indirectClient.getRedirectAction(webContext).getLocation();
                LOGGER.debug("{} -> {}", name, redirectionUrl);
                urls.add(new ProviderLoginPageConfiguration(name, redirectionUrl, name.toLowerCase()));
            } catch (final HttpAction e) {
                if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
                    LOGGER.debug("Authentication request was denied from the provider {}", client.getName());
                } else {
                    LOGGER.warn(e.getMessage(), e);
                }
            } catch (final Exception e) {
                LOGGER.error("Cannot process client {}", client, e);
            }
        }
        if (!urls.isEmpty()) {
            context.getFlowScope().put(PAC4J_URLS, urls);
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No clients could be determined based on the provided configuration");
        }
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name    The name of the parameter
     */
    private static void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session,
                                         final String name) {
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
    private static void saveRequestParameter(final HttpServletRequest request, final HttpSession session,
                                      final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }

    public Clients getClients() {
        return this.clients;
    }

    public void setClients(final Clients clients) {
        this.clients = clients;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return this.authenticationSystemSupport;
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
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
    public static Optional<ModelAndView> hasDelegationRequestFailed(final HttpServletRequest request,
                                                                    final int status) {
        final Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey("error") || params.containsKey("error_code") || params.containsKey("error_description")
                || params.containsKey("error_message")) {
            final Map<String, Object> model = Maps.newHashMap();
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
            model.put("service", request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
            model.put("client", StringEscapeUtils.escapeHtml4(request.getParameter("client_name")));
            
            LOGGER.debug("Delegation request has failed. Details are {}", model);
            return Optional.of(new ModelAndView("casPac4jStopWebflow", model));
        }
        return Optional.empty();
    }

    /**
     * The Provider login page configuration.
     */
    public static class ProviderLoginPageConfiguration implements Serializable {
        private static final long serialVersionUID = 6216882278086699364L;
        private String name;
        private String redirectUrl;
        private String type;

        /**
         * Instantiates a new Provider ui configuration.
         *
         * @param name        the name
         * @param redirectUrl the redirect url
         * @param type        the type
         */
        ProviderLoginPageConfiguration(final String name, final String redirectUrl, final String type) {
            this.name = name;
            this.redirectUrl = redirectUrl;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public String getType() {
            return type;
        }
    }
}
