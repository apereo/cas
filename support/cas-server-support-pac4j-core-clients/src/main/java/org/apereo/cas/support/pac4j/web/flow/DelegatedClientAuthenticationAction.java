package org.apereo.cas.support.pac4j.web.flow;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
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
public class DelegatedClientAuthenticationAction extends AbstractAuthenticationAction {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClientAuthenticationAction.class);

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");
    private static final Pattern PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN = Pattern.compile("\\W");

    private final Clients clients;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final CentralAuthenticationService centralAuthenticationService;
    private final String themeParamName;
    private final String localParamName;
    private final boolean autoRedirect;

    public DelegatedClientAuthenticationAction(
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final Clients clients, final AuthenticationSystemSupport authenticationSystemSupport,
        final CentralAuthenticationService centralAuthenticationService, final String themeParamName,
        final String localParamName, final boolean autoRedirect) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);

        this.clients = clients;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.centralAuthenticationService = centralAuthenticationService;
        this.themeParamName = themeParamName;
        this.localParamName = localParamName;
        this.autoRedirect = autoRedirect;
    }

    @Override
    public Event doExecute(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

        // get client
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        LOGGER.debug("clientName: [{}]", clientName);

        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            return stopWebflow();
        }

        if (StringUtils.isNotBlank(clientName)) {
            final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
            LOGGER.debug("Client: [{}]", client);

            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                LOGGER.debug("Retrieved credentials: [{}]", credentials);
            } catch (final Exception e) {
                LOGGER.debug("The request requires http action", e);
                return stopWebflow();
            }

            final Service service = (Service) session.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            LOGGER.debug("Retrieve service: [{}]", service);
            if (service != null) {
                request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }

            restoreRequestAttribute(request, session, this.themeParamName);
            restoreRequestAttribute(request, session, this.localParamName);
            restoreRequestAttribute(request, session, CasProtocolConstants.PARAMETER_METHOD);

            if (credentials != null) {
                final ClientCredential clientCredential = new ClientCredential(credentials);
                final AuthenticationResult authenticationResult;
                final TicketGrantingTicket tgt;
                try {
                    authenticationResult = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, clientCredential);
                    tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
                } catch (final AuthenticationException | AbstractTicketException e) {
                    LOGGER.warn("Could not establish delegated authentication session [{}]. Routing to [{}]",
                        e.getMessage(), CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
                    return new EventFactorySupport().event(this,
                        CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                        new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, e));
                }
                WebUtils.putTicketGrantingTicketInScopes(context, tgt);
                WebUtils.putAuthentication(authenticationResult.getAuthentication(), context);
                WebUtils.putAuthenticationResult(authenticationResult, context);
                WebUtils.putCredential(context, clientCredential);
                WebUtils.putService(context, service);
                return super.doExecute(context);
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
                try {
                    final ProviderLoginPageConfiguration cfg = urls.stream().findFirst().get();
                    LOGGER.debug("Auto-redirecting to client url [{}]", cfg.getRedirectUrl());
                    response.sendRedirect(cfg.getRedirectUrl());
                    final ExternalContext externalContext = context.getExternalContext();
                    externalContext.recordResponseComplete();
                    return stopWebflow();
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
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

        // web context
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

        // save parameters in web session
        final WebApplicationService service = WebUtils.getService(context);
        LOGGER.debug("Save service: [{}]", service);
        session.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        saveRequestParameter(request, session, this.themeParamName);
        saveRequestParameter(request, session, this.localParamName);
        saveRequestParameter(request, session, CasProtocolConstants.PARAMETER_METHOD);

        final Set<ProviderLoginPageConfiguration> urls = new LinkedHashSet<>();

        this.clients.findAllClients()
            .stream()
            .filter(IndirectClient.class::isInstance)
            .forEach(client -> {
                try {
                    final IndirectClient indirectClient = (IndirectClient) client;

                    final String name = client.getName();
                    final Matcher matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
                    final String type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();

                    final String redirectionUrl;
                    final RedirectAction action = indirectClient.getRedirectAction(webContext);
                    if (RedirectAction.RedirectType.SUCCESS.equals(action.getType())) {
                        redirectionUrl = String.format("javascript:document.write('%1$s');document.close()", action.getContent().replaceAll("\n", ""));
                    } else {
                        redirectionUrl = action.getLocation();
                    }

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
            LOGGER.warn("No clients could be determined based on the provided configuration");
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
        LOGGER.debug("cssClass for {} is {} ", name, computedCssClass);
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
        if (params.containsKey("error") || params.containsKey("error_code") || params.containsKey("error_description")
            || params.containsKey("error_message")) {
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

    /**
     * The Provider login page configuration.
     */
    public static class ProviderLoginPageConfiguration implements Serializable {
        private static final long serialVersionUID = 6216882278086699364L;
        private final String name;
        private final String redirectUrl;
        private final String type;
        private final String cssClass;

        /**
         * Instantiates a new Provider ui configuration.
         *
         * @param name        the name
         * @param redirectUrl the redirect url
         * @param type        the type
         * @param cssClass    for SAML2 clients, the class name used for custom styling of the redirect link
         */
        ProviderLoginPageConfiguration(final String name, final String redirectUrl, final String type, final String cssClass) {
            this.name = name;
            this.redirectUrl = redirectUrl;
            this.type = type;
            this.cssClass = cssClass;
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

        public String getCssClass() {
            return cssClass;
        }
    }
}
