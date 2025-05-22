package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnEvaluator;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.AutomaticFormPostAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);

        val clientName = retrieveClientName(webContext);
        var service = StringUtils.isNotBlank(clientName) ? restoreAuthenticationRequestInContext(context, clientName) : null;

        val clientCredential = extractClientCredential(context, clientName);
        val isLogoutRequest = isLogoutRequest(clientCredential);
        try {
            LOGGER.trace("Delegated authentication is handled by client name [{}]", clientName);
            val isSingleSignOnSessionActive = StringUtils.isNotBlank(clientName)
                && !isLogoutRequest
                && !DelegationWebflowUtils.hasDelegatedClientAuthenticationCandidateProfile(context)
                && ssoEvaluator.singleSignOnSessionExists(context);
            if (isSingleSignOnSessionActive) {
                LOGGER.trace("Found an existing single sign-on session");
                service = populateContextWithService(context, service);
                if (ssoEvaluator.singleSignOnSessionAuthorizedForService(context)) {
                    val providers = configContext.getDelegatedClientIdentityProvidersProducer().produce(context);
                    LOGGER.debug("Skipping delegation and routing back to CAS authentication flow with providers [{}]", providers);
                    return super.doExecuteInternal(context);
                }
                val resolvedService = ssoEvaluator.resolveServiceFromRequestContext(context);
                LOGGER.debug("Single sign-on session is unauthorized for service [{}]", resolvedService);
                removeTicketGrantingTicketIfAny(context, clientName, resolvedService);
            } else if (StringUtils.isNotBlank(clientName) && !isLogoutRequest(clientCredential)) {
                LOGGER.debug("Single sign-on session is inactive for service [{}]", service);
                removeTicketGrantingTicketIfAny(context, clientName, service);
            }

            if (failureEvaluator.evaluate(request, response.getStatus()).isPresent()) {
                throw new IllegalArgumentException("Delegated authentication has failed with client " + clientName);
            }

            if (DelegationWebflowUtils.hasDelegatedClientAuthenticationCandidateProfile(context)) {
                val profile = DelegationWebflowUtils.getDelegatedClientAuthenticationCandidateProfile(context, DelegatedAuthenticationCandidateProfile.class);
                val up = profile.toUserProfile(clientName);
                val clientCredentialSelected = new ClientCredential(clientName, up);
                WebUtils.putCredential(context, clientCredentialSelected);
                return super.doExecuteInternal(context);
            }

            if (clientCredential.isPresent()) {
                service = populateContextWithService(context, service);
                val client = findDelegatedClientByName(clientName, context);
                verifyClientIsAuthorizedForService(context, service, client);
                DelegationWebflowUtils.putDelegatedAuthenticationClientName(context, client.getName());
                if (isLogoutRequest) {
                    val callContext = new CallContext(webContext, configContext.getSessionStore());
                    throw client.processLogout(callContext, clientCredential.get().getCredentials());
                }
                return finalizeDelegatedClientAuthentication(context, clientCredential.get());
            } else if (StringUtils.isNotBlank(clientName)) {
                val msg = "Client %s failed to validate credentials".formatted(clientName);
                LoggingUtils.error(LOGGER, msg);
                return stopWebflow(new AuthenticationException(msg), context);
            }
        } catch (final HttpAction e) {
            FunctionUtils.doIf(LOGGER.isDebugEnabled(),
                o -> LOGGER.debug(e.getMessage(), e), o -> LOGGER.info(e.getMessage())).accept(e);
            val continuation = SingleLogoutContinuation.builder();
            clientCredential.ifPresent(cc -> continuation.context(CollectionUtils.wrap(ClientCredential.class.getName(), cc.getClientName())));
            if (e instanceof final AutomaticFormPostAction formPostAction) {
                continuation.method(HttpMethod.POST).url(formPostAction.getUrl()).data(formPostAction.getData());
            }
            if (e instanceof final WithContentAction withContentAction) {
                continuation.content(withContentAction.getContent());
            }
            webContext.setRequestAttribute(SingleLogoutContinuation.class.getName(), continuation.build());
            return isLogoutRequest(clientCredential) ? getLogoutEvent(e) : success();
        } catch (final UnauthorizedServiceException e) {
            LOGGER.warn(e.getMessage(), e);
            throw e;
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return stopWebflow(e, context);
        }
        return getFinalEvent();
    }

    private void removeTicketGrantingTicketIfAny(final RequestContext context, final String clientName,
                                                 final Service resolvedService) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(context);
        if (tgt != null) {
            configContext.getTicketRegistry().deleteTicket(tgt);
            val client = findDelegatedClientByName(clientName, context);
            verifyClientIsAuthorizedForService(context, resolvedService, client);
        }
    }

    private static boolean isLogoutRequest(final Optional<ClientCredential> clientCredential) {
        return clientCredential.isPresent() && !clientCredential.get().getCredentials().isForAuthentication();
    }

    protected Optional<ClientCredential> extractClientCredential(final RequestContext context, final String clientName) {
        return FunctionUtils.doIfNotBlank(clientName,
            () -> {
                val client = findDelegatedClientByName(clientName, context);
                DelegationWebflowUtils.putDelegatedAuthenticationClientName(context, client.getName());

                val currentCredential = WebUtils.getCredential(context);
                if (currentCredential instanceof final ClientCredential clientCredential) {
                    return Optional.of(clientCredential);
                }
                return populateContextWithClientCredential(client, context);
            },
            Optional::empty);
    }

    protected Event finalizeDelegatedClientAuthentication(final RequestContext context,
                                                          final ClientCredential credentials) throws Throwable {
        val strategies = new ArrayList<>(configContext.getApplicationContext()
            .getBeansOfType(DelegatedClientAuthenticationCredentialResolver.class).values());
        AnnotationAwareOrderComparator.sortIfNecessary(strategies);
        val candidateMatches = strategies
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(strategy -> strategy.supports(credentials))
            .map(Unchecked.function(strategy -> strategy.resolve(context, credentials)))
            .flatMap(List::stream)
            .collect(Collectors.toList());
        if (candidateMatches.isEmpty()) {
            return super.doExecuteInternal(context);
        }
        DelegationWebflowUtils.putDelegatedClientAuthenticationResolvedCredentials(context, candidateMatches);
        return new Event(this, CasWebflowConstants.TRANSITION_ID_SELECT);
    }

    private Event getLogoutEvent(final HttpAction action) {
        return new Event(this, CasWebflowConstants.TRANSITION_ID_LOGOUT,
            new LocalAttributeMap<>("action", action));
    }

    private Event getFinalEvent() {
        return new Event(this, CasWebflowConstants.TRANSITION_ID_GENERATE);
    }


    protected String retrieveClientName(final WebContext webContext) {
        return configContext.getDelegatedClientNameExtractor().extract(webContext).orElse(StringUtils.EMPTY);
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        val replicationProps = configContext.getCasProperties().getAuthn().getPac4j().getCore().getSessionReplication();
        if (replicationProps.isReplicateSessions() && replicationProps.getCookie().isAutoConfigureCookiePath()) {
            val cookieBuilder = configContext.getDelegatedClientDistributedSessionCookieGenerator();
            CookieUtils.configureCookiePath(context, cookieBuilder);
        }
        return super.doPreExecute(context);
    }

    protected Service populateContextWithService(final RequestContext context,
                                                 final Service service) throws Throwable {
        if (service != null) {
            val resolvedService = configContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
            LOGGER.trace("Authentication is resolved by service request from [{}]", service);
            val registeredService = configContext.getServicesManager().findServiceBy(resolvedService);
            LOGGER.trace("Located registered service [{}] mapped to resolved service [{}]", registeredService, resolvedService);
            WebUtils.putRegisteredService(context, registeredService);
            WebUtils.putServiceIntoFlowScope(context, service);
        }
        return service;
    }

    protected Optional<ClientCredential> populateContextWithClientCredential(final BaseClient client,
                                                                             final RequestContext requestContext) {
        return configContext.getCredentialExtractors()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(extractor -> extractor.extract(client, requestContext))
            .flatMap(Optional::stream)
            .findFirst();
    }

    protected BaseClient findDelegatedClientByName(final String clientName, final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        
        val webContext = new JEEContext(request, response);
        val clientResult = configContext.getIdentityProviders().findClient(clientName, webContext);
        if (clientResult.isEmpty()) {
            LOGGER.warn("Delegated client [{}] can not be located", clientName);
            throw UnauthorizedServiceException.denied("Denied: %s".formatted(clientName));
        }
        val client = (BaseClient) clientResult.get();
        client.init();
        return client;
    }

    private void verifyClientIsAuthorizedForService(final RequestContext requestContext, final Service service, final BaseClient client) {
        LOGGER.debug("Delegated authentication client is [{}] with service [{}]", client, service);
        if (service != null) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        }
        if (!isDelegatedClientAuthorizedForService(client, service, requestContext)) {
            LOGGER.error("Delegated client [{}] is not authorized by service [{}]", client, service);
            throw UnauthorizedServiceException.denied("Denied: %s".formatted(service));
        }
    }

    protected Event stopWebflow(final Throwable e, final RequestContext requestContext) {
        requestContext.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, e);
        return new Event(this, CasWebflowConstants.TRANSITION_ID_STOP, new LocalAttributeMap<>("error", e));
    }

    protected Service restoreAuthenticationRequestInContext(final RequestContext requestContext,
                                                            final String givenClientName) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response);
            val clientResult = configContext.getIdentityProviders()
                .findClient(givenClientName, webContext)
                .map(BaseClient.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find client " + givenClientName + " to restore authentication context"));
            return delegatedClientAuthenticationWebflowManager.retrieve(requestContext, webContext, clientResult);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw UnauthorizedServiceException.denied("Denied: %s".formatted(givenClientName));
    }

    protected boolean isDelegatedClientAuthorizedForService(final Client client,
                                                            final Service service,
                                                            final RequestContext requestContext) {
        return configContext.getDelegatedClientIdentityProviderAuthorizers()
            .stream()
            .allMatch(Unchecked.predicate(authz -> authz.isDelegatedClientAuthorizedForService(client, service, requestContext)));
    }
}
