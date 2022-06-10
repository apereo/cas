package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnEvaluator;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

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
     * The configuration context.
     */
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    private final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    private final DelegatedClientAuthenticationFailureEvaluator failureEvaluator;

    private final DelegatedAuthenticationSingleSignOnEvaluator ssoEvaluator;

    public DelegatedClientAuthenticationAction(
        final DelegatedClientAuthenticationConfigurationContext context,
        final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager,
        final DelegatedClientAuthenticationFailureEvaluator failureEvaluator) {
        super(context.getInitialAuthenticationAttemptWebflowEventResolver(),
            context.getServiceTicketRequestWebflowEventResolver(),
            context.getAdaptiveAuthenticationPolicy());
        this.configContext = context;
        this.failureEvaluator = failureEvaluator;
        this.delegatedClientAuthenticationWebflowManager = delegatedClientAuthenticationWebflowManager;
        this.ssoEvaluator = new DelegatedAuthenticationSingleSignOnEvaluator(context);
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
    public Event doExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);

        try {
            val clientName = retrieveClientName(webContext);
            LOGGER.trace("Delegated authentication is handled by client name [{}]", clientName);

            var service = (Service) null;
            if (!isLogoutRequest(request) && ssoEvaluator.singleSignOnSessionExists(context) && StringUtils.isNotBlank(clientName)) {
                LOGGER.trace("Found existing single sign-on session");
                service = populateContextWithService(context, webContext, clientName);
                if (ssoEvaluator.singleSignOnSessionAuthorizedForService(context)) {
                    val providers = configContext.getDelegatedClientIdentityProvidersProducer().produce(context);
                    LOGGER.debug("Skipping delegation and routing back to CAS authentication flow with providers [{}]", providers);
                    return super.doExecute(context);
                }
                val resolvedService = ssoEvaluator.resolveServiceFromRequestContext(context);
                LOGGER.debug("Single sign-on session in unauthorized for service [{}]", resolvedService);
                val tgt = WebUtils.getTicketGrantingTicketId(context);
                configContext.getCentralAuthenticationService().deleteTicket(tgt);
            }

            if (failureEvaluator.evaluate(request, response.getStatus()).isPresent()) {
                throw new IllegalArgumentException("Delegated authentication has failed with client " + clientName);
            }

            if (StringUtils.isNotBlank(clientName)) {
                if (service == null) {
                    service = populateContextWithService(context, webContext, clientName);
                }
                val client = findDelegatedClientByName(context, clientName, service);
                WebUtils.putDelegatedAuthenticationClientName(context, client.getName());
                populateContextWithClientCredential(client, webContext, context);
                return super.doExecute(context);
            }
        } catch (final HttpAction e) {
            FunctionUtils.doIf(LOGGER.isDebugEnabled(),
                o -> LOGGER.debug(e.getMessage(), e), o -> LOGGER.info(e.getMessage())).accept(e);
            JEEHttpActionAdapter.INSTANCE.adapt(e, webContext);
            return isLogoutRequest(request) ? getFinalEvent() : success();
        } catch (final UnauthorizedServiceException e) {
            LOGGER.warn(e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return stopWebflow(e, context);
        }
        return getFinalEvent();
    }

    private Event getFinalEvent() {
        return new Event(this, CasWebflowConstants.TRANSITION_ID_GENERATE);
    }

    /**
     * Retrieve the client name when calling back the CAS server after the delegated authentication.
     *
     * @param webContext the web context
     * @return the client name
     */
    protected String retrieveClientName(final WebContext webContext) {
        return configContext.getDelegatedClientNameExtractor().extract(webContext).orElse(StringUtils.EMPTY);
    }

  
    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        val casProperties = configContext.getCasProperties();
        if (casProperties.getAuthn().getPac4j().getCore().isReplicateSessions()
            && casProperties.getSessionReplication().getCookie().isAutoConfigureCookiePath()) {

            val contextPath = context.getExternalContext().getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val path = configContext.getDelegatedClientDistributedSessionCookieGenerator().getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for distributed session cookie generator to: [{}]", cookiePath);
                configContext.getDelegatedClientDistributedSessionCookieGenerator().setCookiePath(cookiePath);
            } else {
                LOGGER.trace("Delegated authentication cookie domain is [{}] with path [{}]",
                    configContext.getDelegatedClientDistributedSessionCookieGenerator().getCookieDomain(), path);
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
    protected void populateContextWithClientCredential(final BaseClient client, final JEEContext webContext,
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
    protected Credentials getCredentialsFromDelegatedClient(final JEEContext webContext, final BaseClient client) {
        val credentials = client.getCredentials(webContext, configContext.getSessionStore());
        LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
        if (credentials.isEmpty()) {
            throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
        }
        return credentials.get();
    }

    /**
     * Find delegated client by name base client.
     *
     * @param requestContext the request context
     * @param clientName     the client name
     * @param service        the service
     * @return the base client
     */
    protected BaseClient findDelegatedClientByName(final RequestContext requestContext,
                                                   final String clientName, final Service service) {
        val clientResult = configContext.getClients().findClient(clientName);
        if (clientResult.isEmpty()) {
            LOGGER.warn("Delegated client [{}] can not be located", clientName);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        val client = BaseClient.class.cast(clientResult.get());
        LOGGER.debug("Delegated authentication client is [{}] with service [{}]", client, service);
        if (service != null) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
        if (!isDelegatedClientAuthorizedForService(client, service, requestContext)) {
            LOGGER.warn("Delegated client [{}] is not authorized by service [{}]", client, service);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        client.init();
        return client;
    }

    /**
     * Stop webflow event.
     *
     * @param e              the exception
     * @param requestContext the request context
     * @return the event
     */
    protected Event stopWebflow(final Exception e, final RequestContext requestContext) {
        requestContext.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, e);
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
                return delegatedClientAuthenticationWebflowManager.retrieve(requestContext,
                    webContext, BaseClient.class.cast(clientResult.get()));
            }
            LOGGER.warn("Unable to locate client [{}] in registered clients", clientName);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
    }

    protected boolean isDelegatedClientAuthorizedForService(final Client client,
                                                            final Service service,
                                                            final RequestContext requestContext) {
        return configContext.getDelegatedClientIdentityProviderAuthorizers()
            .stream()
            .allMatch(authz -> authz.isDelegatedClientAuthorizedForService(client, service, requestContext));
    }
}
