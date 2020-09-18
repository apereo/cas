package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;
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
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    /**
     * Instantiates a new Delegated client authentication action.
     *
     * @param context the context
     */
    public DelegatedClientAuthenticationAction(final DelegatedClientAuthenticationConfigurationContext context) {
        super(context.getInitialAuthenticationAttemptWebflowEventResolver(),
            context.getServiceTicketRequestWebflowEventResolver(),
            context.getAdaptiveAuthenticationPolicy());
        this.configContext = context;
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

    @Override
    public Event doExecute(final RequestContext context) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            val webContext = new JEEContext(request, response, configContext.getSessionStore());

            val clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
            LOGGER.trace("Delegated authentication is handled by client name [{}]", clientName);

            var service = (Service) null;
            if (!isLogoutRequest(request) && singleSignOnSessionExists(context) && StringUtils.isNotBlank(clientName)) {
                LOGGER.trace("Found existing single sign-on session");
                service = populateContextWithService(context, webContext, clientName);
                if (singleSignOnSessionAuthorizedForService(context)) {
                    val providers = configContext.getDelegatedClientIdentityProvidersFunction().apply(context);
                    LOGGER.trace("Skipping delegation and routing back to CAS authentication flow with providers [{}]", providers);
                    return super.doExecute(context);
                }
                val resolvedService = resolveServiceFromRequestContext(context);
                LOGGER.debug("Single sign-on session in unauthorized for service [{}]", resolvedService);
                val tgt = WebUtils.getTicketGrantingTicketId(context);
                configContext.getCentralAuthenticationService().deleteTicket(tgt);
            }

            if (hasDelegationRequestFailed(request, response.getStatus()).isPresent()) {
                throw new IllegalArgumentException("Delegated authentication has failed with client " + clientName);
            }

            if (StringUtils.isNotBlank(clientName)) {
                if (service == null) {
                    service = populateContextWithService(context, webContext, clientName);
                }
                val client = findDelegatedClientByName(request, clientName, service);
                populateContextWithClientCredential(client, webContext, context);
                return super.doExecute(context);
            }

            val providers = configContext.getDelegatedClientIdentityProvidersFunction().apply(context);
            LOGGER.trace("Delegated authentication providers are finalized as [{}]", providers);
            WebUtils.createCredential(context);
            if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                throw new UnauthorizedAuthenticationException("Authentication is not authorized: " + response.getStatus());
            }
        } catch (final UnauthorizedServiceException e) {
            LOGGER.warn(e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return stopWebflow(e, context);
        }
        return error();
    }

    private Service resolveServiceFromRequestContext(final RequestContext context) {
        val service = WebUtils.getService(context);
        return configContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
    }

    private Optional<Authentication> getSingleSignOnAuthenticationFrom(final RequestContext requestContext) {
        val tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgtId)) {
            LOGGER.trace("No ticket-granting ticket could be located in the webflow context");
            return Optional.empty();
        }
        val ticket = configContext.getCentralAuthenticationService().getTicket(tgtId, TicketGrantingTicket.class);
        if (ticket != null && !ticket.isExpired()) {
            LOGGER.trace("Located a valid ticket-granting ticket");
            return Optional.of(ticket.getAuthentication());
        }
        return Optional.empty();
    }

    private boolean isDelegatedClientAuthorizedForService(final Client<Credentials> client,
                                                          final Service service) {
        return configContext.getDelegatedAuthenticationAccessStrategyHelper()
            .isDelegatedClientAuthorizedForService(client, service);
    }

    /**
     * Is this a SAML logout request?
     *
     * @param request the HTTP request
     * @return whether it is a SAML logout request
     */
    protected static boolean isLogoutRequest(final HttpServletRequest request) {
        return request.getParameter(Pac4jConstants.LOGOUT_ENDPOINT_PARAMETER) != null;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        if (configContext.getCasProperties().getAuthn().getPac4j().isReplicateSessions()
            && configContext.getCasProperties().getSessionReplication().getCookie().isAutoConfigureCookiePath()) {

            val contextPath = context.getExternalContext().getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val path = configContext.getCookieGenerator().getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for distributed session cookie generator to: [{}]", cookiePath);
                configContext.getCookieGenerator().setCookiePath(cookiePath);
            } else {
                LOGGER.trace("Delegated authentication cookie domain is [{}] with path [{}]", configContext.getCookieGenerator().getCookieDomain(), path);
            }
        }
        return super.doPreExecute(context);
    }

    /**
     * Populate context with service service.
     *
     * @param context    the context
     * @param webContext the web context
     * @param clientName the client name
     * @return the service
     */
    protected Service populateContextWithService(final RequestContext context, final JEEContext webContext, final String clientName) {
        val service = restoreAuthenticationRequestInContext(context, webContext, clientName);
        val resolvedService = configContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
        LOGGER.trace("Authentication is resolved by service request from [{}]", service);
        val registeredService = configContext.getServicesManager().findServiceBy(resolvedService);
        LOGGER.trace("Located registered service [{}] mapped to resolved service [{}]", registeredService, resolvedService);
        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, service);
        return service;
    }

    /**
     * Populate context with client credential.
     *
     * @param client         the client
     * @param webContext     the web context
     * @param requestContext the request context
     */
    protected void populateContextWithClientCredential(final BaseClient<Credentials> client, final JEEContext webContext,
                                                       final RequestContext requestContext) {
        LOGGER.debug("Fetching credentials from delegated client [{}]", client);
        val credentials = getCredentialsFromDelegatedClient(webContext, client);
        val clientCredential = new ClientCredential(credentials, client.getName());
        LOGGER.info("Credentials are successfully authenticated using the delegated client [{}]", client.getName());
        WebUtils.putCredential(requestContext, clientCredential);
    }

    /**
     * Gets credentials from delegated client.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the credentials from delegated client
     */
    protected Credentials getCredentialsFromDelegatedClient(final JEEContext webContext, final BaseClient<Credentials> client) {
        val credentials = client.getCredentials(webContext);
        LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
        if (credentials.isEmpty()) {
            throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
        }
        return credentials.get();
    }

    /**
     * Find delegated client by name base client.
     *
     * @param request    the request
     * @param clientName the client name
     * @param service    the service
     * @return the base client
     */
    protected BaseClient<Credentials> findDelegatedClientByName(final HttpServletRequest request,
                                                                final String clientName, final Service service) {
        val clientResult = configContext.getClients().findClient(clientName);
        if (clientResult.isEmpty()) {
            LOGGER.warn("Delegated client [{}] can not be located", clientName);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        val client = BaseClient.class.cast(clientResult.get());
        LOGGER.debug("Delegated authentication client is [{}] with service [{}]", client, service);
        if (service != null) {
            request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            if (!isDelegatedClientAuthorizedForService(client, service)) {
                LOGGER.warn("Delegated client [{}] is not authorized by service [{}]", client, service);
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
            }
        }
        client.init();
        return client;
    }

    /**
     * Stop webflow event.
     *
     * @param e              the e
     * @param requestContext the request context
     * @return the event
     */
    protected Event stopWebflow(final Exception e, final RequestContext requestContext) {
        requestContext.getFlashScope().put("rootCauseException", e);
        return new Event(this, CasWebflowConstants.TRANSITION_ID_STOP, new LocalAttributeMap<>("error", e));
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
                                                            final JEEContext webContext,
                                                            final String clientName) {
        val logoutEndpoint = isLogoutRequest(webContext.getNativeRequest());
        if (logoutEndpoint) {
            return null;
        }
        try {
            val clientResult = configContext.getClients().findClient(clientName);
            if (clientResult.isPresent()) {
                return configContext.getDelegatedClientWebflowManager()
                    .retrieve(requestContext, webContext, BaseClient.class.cast(clientResult.get()));
            }
            LOGGER.warn("Unable to locate client [{}] in registered clients", clientName);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
    }

    /**
     * Single sign on session authorized for service boolean.
     *
     * @param context the context
     * @return the boolean
     */
    protected boolean singleSignOnSessionAuthorizedForService(final RequestContext context) {
        val resolvedService = resolveServiceFromRequestContext(context);
        val authentication = getSingleSignOnAuthenticationFrom(context);
        return authentication
            .map(authn -> configContext.getDelegatedAuthenticationAccessStrategyHelper()
                .isDelegatedClientAuthorizedForAuthentication(authn, resolvedService))
            .orElse(Boolean.FALSE);
    }

    /**
     * Is there a current SSO session?
     *
     * @param requestContext the request context
     * @return whether there is a current SSO session
     */
    protected boolean singleSignOnSessionExists(final RequestContext requestContext) {
        try {
            val authn = getSingleSignOnAuthenticationFrom(requestContext);
            if (authn.isPresent()) {
                LOGGER.trace("Located a valid ticket-granting ticket. Examining existing single sign-on session strategies...");
                val authentication = authn.get();
                val builder = configContext.getAuthenticationSystemSupport()
                    .establishAuthenticationContextFromInitial(authentication);
                LOGGER.trace("Recording and tracking initial authentication results in the request context");
                WebUtils.putAuthenticationResultBuilder(builder, requestContext);
                WebUtils.putAuthentication(authentication, requestContext);

                val strategy = configContext.getSingleSignOnParticipationStrategy();
                return strategy.supports(requestContext) && strategy.isParticipating(requestContext);
            }
        } catch (final AbstractTicketException e) {
            LOGGER.trace("Could not retrieve ticket id [{}] from registry.", e.getMessage());
        }
        LOGGER.trace("Ticket-granting ticket found in the webflow context is invalid or has expired");
        return false;
    }
}
