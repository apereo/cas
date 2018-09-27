package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class DelegatedClientAuthenticationAction extends AbstractAuthenticationAction {
    /**
     * All the urls and names of the pac4j clients.
     */
    public static final String PAC4J_URLS = "pac4jUrls";

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
     * The Delegated session cookie manager.
     */
    protected final DelegatedSessionCookieManager delegatedSessionCookieManager;
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

    public DelegatedClientAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                               final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                               final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                               final Clients clients,
                                               final ServicesManager servicesManager,
                                               final AuditableExecution delegatedAuthenticationPolicyEnforcer,
                                               final DelegatedClientWebflowManager delegatedClientWebflowManager,
                                               final DelegatedSessionCookieManager delegatedSessionCookieManager,
                                               final AuthenticationSystemSupport authenticationSystemSupport,
                                               final String localeParamName,
                                               final String themeParamName,
                                               final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.clients = clients;
        this.servicesManager = servicesManager;
        this.delegatedAuthenticationPolicyEnforcer = delegatedAuthenticationPolicyEnforcer;
        this.delegatedClientWebflowManager = delegatedClientWebflowManager;
        this.delegatedSessionCookieManager = delegatedSessionCookieManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.localeParamName = localeParamName;
        this.themeParamName = themeParamName;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
    }

    @Override
    public Event doExecute(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        final String clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        LOGGER.debug("Delegated authentication is handled by client name [{}]", clientName);
        if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
            throw new IllegalArgumentException("Delegated authentication has failed with client " + clientName);
        }

        final J2EContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);
        if (StringUtils.isNotBlank(clientName)) {
            final Service service = restoreAuthenticationRequestInContext(context, webContext, clientName);
            final BaseClient<Credentials, CommonProfile> client = findDelegatedClientByName(request, clientName, service);

            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
                if (credentials == null) {
                    throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
                }
            } catch (final Exception e) {
                LOGGER.info(e.getMessage(), e);
                throw new IllegalArgumentException("Delegated authentication has failed with client " + client.getName());
            }

            final ClientCredential clientCredential = new ClientCredential(credentials, client.getName());
            WebUtils.putCredential(context, clientCredential);
            WebUtils.putService(context, service);
            return super.doExecute(context);
        }

        prepareForLoginPage(context);

        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            return stopWebflow();
        }
        return error();
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
        final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
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
        final Service currentService = WebUtils.getService(context);
        final WebApplicationService service = authenticationRequestServiceSelectionStrategies.resolveService(currentService, WebApplicationService.class);

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final WebContext webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

        final Set<ProviderLoginPageConfiguration> urls = new LinkedHashSet<>();
        this.clients.findAllClients()
            .stream()
            .filter(client -> client instanceof IndirectClient && isDelegatedClientAuthorizedForService(client, service))
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                try {
                    final Optional<ProviderLoginPageConfiguration> provider = buildProviderConfiguration(client, webContext, service);
                    provider.ifPresent(urls::add);
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
     * Build provider configuration optional.
     *
     * @param client     the client
     * @param webContext the web context
     * @param service    the service
     * @return the optional
     */
    protected Optional<ProviderLoginPageConfiguration> buildProviderConfiguration(final IndirectClient client, final WebContext webContext,
                                                                                  final WebApplicationService service) {
        final String name = client.getName();
        final Matcher matcher = PAC4J_CLIENT_SUFFIX_PATTERN.matcher(client.getClass().getSimpleName());
        final String type = matcher.replaceAll(StringUtils.EMPTY).toLowerCase();
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromUriString(DelegatedClientNavigationController.ENDPOINT_REDIRECT)
            .queryParam(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, name);

        if (service != null) {
            final String sourceParam = service.getSource();
            final String serviceParam = service.getOriginalUrl();
            if (StringUtils.isNotBlank(sourceParam) && StringUtils.isNotBlank(serviceParam)) {
                uriBuilder.queryParam(sourceParam, serviceParam);
            }
        }

        final String methodParam = webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(methodParam)) {
            uriBuilder.queryParam(CasProtocolConstants.PARAMETER_METHOD, methodParam);
        }
        final String localeParam = webContext.getRequestParameter(this.localeParamName);
        if (StringUtils.isNotBlank(localeParam)) {
            uriBuilder.queryParam(this.localeParamName, localeParam);
        }
        final String themeParam = webContext.getRequestParameter(this.themeParamName);
        if (StringUtils.isNotBlank(themeParam)) {
            uriBuilder.queryParam(this.themeParamName, themeParam);
        }
        final String redirectUrl = uriBuilder.toUriString();
        final boolean autoRedirect = (Boolean) client.getCustomProperties().getOrDefault(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.FALSE);
        final ProviderLoginPageConfiguration p = new ProviderLoginPageConfiguration(name, redirectUrl, type, getCssClass(name), autoRedirect);
        return Optional.of(p);
    }


    /**
     * Get a valid CSS class for the given provider name.
     *
     * @param name Name of the provider
     * @return the css class
     */
    protected String getCssClass(final String name) {
        String computedCssClass = "fa fa-lock";
        if (StringUtils.isNotBlank(name)) {
            computedCssClass = computedCssClass.concat(' ' + PAC4J_CLIENT_CSS_CLASS_SUBSTITUTION_PATTERN.matcher(name).replaceAll("-"));
        }
        LOGGER.debug("cssClass for [{}] is [{}]", name, computedCssClass);
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

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("Service access for [{}] is denied", registeredService);
            return false;
        }
        LOGGER.debug("Located registered service definition [{}] matching [{}]", registeredService, service);
        final AuditableContext context = AuditableContext.builder()
            .registeredService(registeredService)
            .properties(CollectionUtils.wrap(Client.class.getSimpleName(), client.getName()))
            .build();
        final AuditableExecutionResult result = delegatedAuthenticationPolicyEnforcer.execute(context);
        if (!result.isExecutionFailure()) {
            LOGGER.debug("Delegated authentication policy for [{}] allows for using client [{}]", registeredService, client);
            return true;
        }
        LOGGER.warn("Delegated authentication policy for [{}] refuses access to client [{}]", registeredService.getServiceId(), client);
        return false;
    }

    /**
     * Restore authentication request in context service.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param clientName     the client name
     * @return the service
     */
    protected Service restoreAuthenticationRequestInContext(final RequestContext requestContext, final J2EContext webContext, final String clientName) {
        try {
            delegatedSessionCookieManager.restore(webContext);
            final BaseClient<Credentials, CommonProfile> client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
            final Service service = delegatedClientWebflowManager.retrieve(requestContext, webContext, client);
            return service;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized");
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
