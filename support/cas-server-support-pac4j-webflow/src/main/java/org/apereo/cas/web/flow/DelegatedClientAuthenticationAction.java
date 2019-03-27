package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.pac4j.logout.RequestSloException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.saml.metadata.SAML2ServiceProviderMetadataResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
public class DelegatedClientAuthenticationAction extends AbstractAuthenticationAction {
    /**
     * All the urls and names of the provider clients.
     */
    public static final String FLOW_ATTRIBUTE_PROVIDER_URLS = "delegatedAuthenticationProviderUrls";

    private static final Pattern PAC4J_CLIENT_SUFFIX_PATTERN = Pattern.compile("Client\\d*");
    private static final Pattern PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN = Pattern.compile("\\W");

    /**
     * The Clients.
     */
    protected final Clients clients;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Delegated authentication policy enforcer.
     */
    protected final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    /**
     * The Delegated client webflow manager.
     */
    protected final DelegatedClientWebflowManager delegatedClientWebflowManager;

    /**
     * The Authentication system support.
     */
    protected final AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * The Locale param name.
     */
    protected final String localeParamName;

    /**
     * The Theme param name.
     */
    protected final String themeParamName;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CentralAuthenticationService centralAuthenticationService;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final SessionStore sessionStore;

    public DelegatedClientAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                               final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                               final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                               final Clients clients,
                                               final ServicesManager servicesManager,
                                               final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                               final DelegatedClientWebflowManager delegatedClientWebflowManager,
                                               final AuthenticationSystemSupport authenticationSystemSupport,
                                               final String localeParamName,
                                               final String themeParamName,
                                               final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                               final CentralAuthenticationService centralAuthenticationService,
                                               final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy,
                                               final SessionStore sessionStore) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.clients = clients;
        this.servicesManager = servicesManager;
        this.delegatedAuthenticationPolicyEnforcer = delegatedAuthenticationPolicyEnforcer;
        this.delegatedClientWebflowManager = delegatedClientWebflowManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.localeParamName = localeParamName;
        this.themeParamName = themeParamName;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.centralAuthenticationService = centralAuthenticationService;
        this.singleSignOnParticipationStrategy = singleSignOnParticipationStrategy;
        this.sessionStore = sessionStore;
    }


    @Override
    public Event doExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        if (singleSignOnSessionExists(context)) {
            LOGGER.trace("An existing single sign-on session already exists. Skipping delegation and routing back to CAS authentication flow");
            return super.doExecute(context);
        }

        val clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        LOGGER.trace("Delegated authentication is handled by client name [{}]", clientName);
        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            throw new IllegalArgumentException("Delegated authentication has failed with client " + clientName);
        }

        val webContext = Pac4jUtils.getPac4jJ2EContext(request, response, this.sessionStore);
        if (StringUtils.isNotBlank(clientName)) {
            val service = restoreAuthenticationRequestInContext(context, webContext, clientName);
            val client = findDelegatedClientByName(request, clientName, service);

            try {
                LOGGER.debug("Fetching credentials from delegated client [{}]", client);
                val credentials = getCredentialsFromDelegatedClient(webContext, client);
                val clientCredential = new ClientCredential(credentials, clientName);
                LOGGER.info("Credentials are successfully authenticated using the delegated client [{}]", clientName);
                WebUtils.putCredential(context, clientCredential);
                WebUtils.putServiceIntoFlowScope(context, service);
                LOGGER.trace("Authentication is resolved by service request from [{}]", service);
                val resolvedService = authenticationRequestServiceSelectionStrategies.resolveService(service);
                val registeredService = servicesManager.findServiceBy(resolvedService);
                LOGGER.trace("Located registered service [{}] mapped to resolved service [{}]", registeredService, resolvedService);
                WebUtils.putRegisteredService(context, registeredService);
            } catch (final Exception e) {
                return handleException(webContext, client, e);
            }
            return super.doExecute(context);
        }

        prepareForLoginPage(context);

        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            return stopWebflow();
        }
        return error();
    }

    /**
     * Handle the thrown exception.
     *
     * @param webContext the web context
     * @param client     the authentication client
     * @param e          the thrown exception
     * @return the event to trigger
     */
    protected Event handleException(final J2EContext webContext, final BaseClient<Credentials, CommonProfile> client, final Exception e) {
        if (e instanceof RequestSloException) {
            try {
                webContext.getResponse().sendRedirect("logout");
            } catch (final IOException ioe) {
                throw new IllegalArgumentException("Unable to call logout", ioe);
            }
            return stopWebflow();
        }
        val msg = String.format("Delegated authentication has failed with client %s", client.getName());
        LOGGER.error(msg, e);
        throw new IllegalArgumentException(msg);
    }

    private boolean singleSignOnSessionExists(final RequestContext requestContext) {
        val tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgtId)) {
            LOGGER.trace("No ticket-granting ticket could be located in the webflow context");
            return false;
        }
        try {
            val ticket = this.centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            if (ticket != null && !ticket.isExpired()) {
                LOGGER.trace("Located a valid ticket-granting ticket. Examining existing single sign-on session strategies...");

                val authentication = ticket.getAuthentication();
                val builder = this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
                LOGGER.trace("Recording and tracking initial authentication results in the request context");
                WebUtils.putAuthenticationResultBuilder(builder, requestContext);
                WebUtils.putAuthentication(authentication, requestContext);

                return singleSignOnParticipationStrategy.supports(requestContext)
                    && singleSignOnParticipationStrategy.isParticipating(requestContext);
            }
        } catch (final AbstractTicketException e) {
            LOGGER.trace("Could not retrieve ticket id [{}] from registry.", e.getMessage());
        }
        LOGGER.trace("Ticket-granting ticket found in the webflow context is invalid or has expired");
        return false;
    }

    /**
     * Gets credentials from delegated client.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the credentials from delegated client
     */
    protected Credentials getCredentialsFromDelegatedClient(final J2EContext webContext, final BaseClient<Credentials, CommonProfile> client) {
        val credentials = client.getCredentials(webContext);
        LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
        if (credentials == null) {
            throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
        }
        return credentials;
    }

    /**
     * Find delegated client by name base client.
     *
     * @param request    the request
     * @param clientName the client name
     * @param service    the service
     * @return the base client
     */
    protected BaseClient<Credentials, CommonProfile> findDelegatedClientByName(final HttpServletRequest request, final String clientName, final Service service) {
        val client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
        LOGGER.debug("Delegated authentication client is [{}] with service [{}}", client, service);
        if (service != null) {
            request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            if (!isDelegatedClientAuthorizedForService(client, service)) {
                LOGGER.warn("Delegated client [{}] is not authorized by service [{}]", client, service);
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
            }
        }
        return client;
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     */
    protected void prepareForLoginPage(final RequestContext context) {
        val currentService = WebUtils.getService(context);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(currentService, WebApplicationService.class);

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = Pac4jUtils.getPac4jJ2EContext(request, response, this.sessionStore);

        val urls = new LinkedHashSet<ProviderLoginPageConfiguration>();
        this.clients
            .findAllClients()
            .stream()
            .filter(client -> client instanceof IndirectClient && isDelegatedClientAuthorizedForService(client, service))
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                try {
                    val provider = buildProviderConfiguration(client, webContext, currentService);
                    provider.ifPresent(p -> {
                        urls.add(p);
                        if (p.isAutoRedirect()) {
                            WebUtils.putDelegatedAuthenticationProviderDominant(context, p);
                        }
                    });
                } catch (final Exception e) {
                    LOGGER.error("Cannot process client [{}]", client, e);
                }
            });

        if (!urls.isEmpty()) {
            context.getFlowScope().put(FLOW_ATTRIBUTE_PROVIDER_URLS, urls);
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no clients are configured, or the current access strategy rules prohibit CAS from using authentication providers for this request.");
        }
    }

    /**
     * Build provider configuration optional.
     *
     * @param client     the client
     * @param webContext the web context
     * @param service    the service
     * @return the optional
     */
    protected Optional<ProviderLoginPageConfiguration> buildProviderConfiguration(final IndirectClient client, final WebContext webContext,
                                                                                  final WebApplicationService service) {
        val name = client.getName();
        val matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
        val type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();
        val uriBuilder = UriComponentsBuilder
            .fromUriString(DelegatedClientNavigationController.ENDPOINT_REDIRECT)
            .queryParam(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, name);

        if (service != null) {
            val sourceParam = service.getSource();
            val serviceParam = service.getOriginalUrl();
            if (StringUtils.isNotBlank(sourceParam) && StringUtils.isNotBlank(serviceParam)) {
                uriBuilder.queryParam(sourceParam, serviceParam);
            }
        }

        val methodParam = webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methodParam)) {
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, methodParam);
        }
        val localeParam = webContext.getRequestParameter(this.localeParamName);
        if (StringUtils.isNotBlank(localeParam)) {
            uriBuilder.queryParam(this.localeParamName, localeParam);
        }
        val themeParam = webContext.getRequestParameter(this.themeParamName);
        if (StringUtils.isNotBlank(themeParam)) {
            uriBuilder.queryParam(this.themeParamName, themeParam);
        }
        val redirectUrl = uriBuilder.toUriString();
        val autoRedirect = (Boolean) client.getCustomProperties().getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.FALSE);
        val p = new ProviderLoginPageConfiguration(name, redirectUrl, type, getCssClass(name), autoRedirect);
        return Optional.of(p);
    }

    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param name Name of the provider
     * @return the css class
     */
    protected String getCssClass(final String name) {
        var computedCssClass = "fa fa-lock";
        if (StringUtils.isNotBlank(name)) {
            computedCssClass = computedCssClass.concat(' ' + PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN.matcher(name).replaceAll("-"));
        }
        LOGGER.debug("CSS class for [{}] is [{}]", name, computedCssClass);
        return computedCssClass;
    }

    /**
     * Stop webflow event.
     *
     * @return the event
     */
    protected Event stopWebflow() {
        return new Event(this, CasWebflowConstants.TRANSITION_ID_STOP);
    }

    /**
     * Is delegated client authorized for service boolean.
     *
     * @param client  the client
     * @param service the service
     * @return the boolean
     */
    protected boolean isDelegatedClientAuthorizedForService(final Client client, final Service service) {
        if (service == null || StringUtils.isBlank(service.getId())) {
            LOGGER.debug("Can not evaluate delegated authentication policy since no service was provided in the request while processing client [{}]", client);
            return true;
        }

        val registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("Service access for [{}] is denied", registeredService);
            return false;
        }
        LOGGER.trace("Located registered service definition [{}] matching [{}]", registeredService, service);
        val context = AuditableContext.builder()
            .registeredService(registeredService)
            .properties(CollectionUtils.wrap(Client.class.getSimpleName(), client.getName()))
            .build();
        val result = delegatedAuthenticationPolicyEnforcer.execute(context);
        if (!result.isExecutionFailure()) {
            LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, client);
            return true;
        }
        LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), client);
        return false;
    }

    /**
     * Restore authentication request in context service (return null for a logout call).
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param clientName     the client name
     * @return the service
     */
    protected Service restoreAuthenticationRequestInContext(final RequestContext requestContext,
                                                            final J2EContext webContext,
                                                            final String clientName) {
        val logoutEndpoint = webContext.getRequestParameter(SAML2ServiceProviderMetadataResolver.LOGOUT_ENDPOINT_PARAMETER);
        if (logoutEndpoint != null) {
            return null;
        } else {
            try {
                val client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
                return delegatedClientWebflowManager.retrieve(requestContext, webContext, client);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized");
        }
    }

    /**
     * Determine if request has errors.
     *
     * @param request the request
     * @param status  the status
     * @return the optional model and view, if request is an error.
     */
    public static Optional<ModelAndView> hasDelegationRequestFailed(final HttpServletRequest request, final int status) {
        val params = request.getParameterMap();
        if (Stream.of("error", "error_code", "error_description", "error_message").anyMatch(params::containsKey)) {
            val model = new HashMap<String, Object>();
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
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class ProviderLoginPageConfiguration implements Serializable {

        private static final long serialVersionUID = 6216882278086699364L;

        private final String name;
        private final String redirectUrl;
        private final String type;
        private final String cssClass;
        private final boolean autoRedirect;
    }
}
