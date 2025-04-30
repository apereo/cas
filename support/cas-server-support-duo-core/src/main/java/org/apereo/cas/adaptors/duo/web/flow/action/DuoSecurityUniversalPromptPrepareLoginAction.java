package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import com.duosecurity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.scope.ConversationScope;
import org.springframework.webflow.scope.FlashScope;
import org.springframework.webflow.scope.FlowScope;
import org.springframework.webflow.scope.RequestScope;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DuoSecurityUniversalPromptPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DuoSecurityUniversalPromptPrepareLoginAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {
    protected final BrowserWebStorageSessionStore duoUniversalPromptSessionStore;
    protected final TicketRegistry ticketRegistry;
    protected final TicketFactory ticketFactory;
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val authentication = WebUtils.getAuthentication(requestContext);
        val duoSecurityIdentifier = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(requestContext);

        val duoProvider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(duoSecurityIdentifier, applicationContext)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .orElseThrow(() -> new IllegalArgumentException("Unable to locate multifactor authentication provider by id " + duoSecurityIdentifier));

        val client = getDuoSecurityClient(duoProvider, duoSecurityIdentifier);
        val state = client.generateState();
        val service = WebUtils.getService(requestContext);
        LOGGER.debug("Generated Duo Security state [{}] for service [{}]", state, service);

        val properties = new LinkedHashMap<String, Object>();
        properties.put("duoProviderId", duoSecurityIdentifier);
        properties.put(Authentication.class.getSimpleName(), authentication);
        properties.put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION, authentication);
        val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(requestContext);
        properties.put(AuthenticationResultBuilder.class.getSimpleName(), authenticationResultBuilder);
        properties.put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER, authenticationResultBuilder);
        val authenticationResult = WebUtils.getAuthenticationResult(requestContext);
        properties.put(AuthenticationResult.class.getSimpleName(), authenticationResult);
        properties.put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT, authenticationResult);
        properties.put(Credential.class.getSimpleName(), MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationParentCredential(requestContext));
        FunctionUtils.doIfNotNull(service, __ -> properties.put(Service.class.getSimpleName(), service));
        properties.put(DuoSecurityAuthenticationService.class.getSimpleName(), state);

        val targetState = WebUtils.getTargetTransition(requestContext);
        FunctionUtils.doIfNotNull(targetState, __ -> properties.put(CasWebflowConstants.ATTRIBUTE_TARGET_TRANSITION, targetState));

        properties.put(FlowScope.class.getSimpleName(), requestContext.getFlowScope().asMap());
        properties.put(FlashScope.class.getSimpleName(), requestContext.getFlashScope().asMap());
        properties.put(ConversationScope.class.getSimpleName(), requestContext.getConversationScope().asMap());
        properties.put(RequestScope.class.getSimpleName(), requestContext.getRequestScope().asMap());

        Optional.ofNullable(WebUtils.getRegisteredService(requestContext))
            .ifPresent(registeredService -> properties.put(RegisteredService.class.getSimpleName(), registeredService));

        val urlWithStatePair = createAuthUrlWithState(duoProvider, properties, requestContext, state);
        requestContext.getFlowScope().put("duoUniversalPromptLoginUrl", urlWithStatePair.getLeft());

        if (!provider.getDuoAuthenticationService().getProperties().getSessionStorageType().isTicketRegistry()) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val context = new JEEContext(request, response);
            LOGGER.debug("Storing Duo Security session attributes [{}] into session", properties);
            val sessionStorage = duoUniversalPromptSessionStore
                .withSessionAttributes(context, properties)
                .getTrackableSession(context)
                .map(BrowserStorage.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to determine trackable session for storage"));
            sessionStorage.setDestinationUrl(urlWithStatePair.getLeft());
            WebUtils.putBrowserStorage(requestContext, sessionStorage);
            return success(sessionStorage);
        }
        LOGGER.debug("Redirecting to Duo Security url at [{}]", urlWithStatePair);
        return success(urlWithStatePair.getRight());
    }

    protected Client getDuoSecurityClient(final DuoSecurityMultifactorAuthenticationProvider duoProvider,
                                          final String duoSecurityIdentifier) {
        return duoProvider.getDuoAuthenticationService()
            .getDuoClient()
            .map(Client.class::cast)
            .orElseThrow(() -> new RuntimeException("Unable to locate Duo Security client for provider id " + duoSecurityIdentifier));
    }

    protected Pair<String, String> createAuthUrlWithState(final DuoSecurityMultifactorAuthenticationProvider provider,
                                                          final Map<String, Object> properties,
                                                          final RequestContext requestContext,
                                                          final String state) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        LOGGER.debug("Principal resolved for Duo Security as [{}]", principal);
        var principalId = principal.getId();
        val principalAttribute = provider.getDuoAuthenticationService().getProperties().getPrincipalAttribute();
        if (principal.getAttributes().containsKey(principalAttribute)) {
            principalId = principal.getAttributes().get(principalAttribute).getFirst().toString();
        }

        val duoSecurityIdentifier = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(requestContext);
        val client = getDuoSecurityClient(provider, duoSecurityIdentifier);
        var effectiveState = state;
        if (provider.getDuoAuthenticationService().getProperties().getSessionStorageType().isTicketRegistry()) {
            val service = WebUtils.getService(requestContext);
            val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
            val ticket = factory.create(state, service, properties);
            ticketRegistry.addTicket(ticket);
            effectiveState = ticket.getId();
            LOGGER.debug("Stored Duo Security session via [{}]", ticket);
        }

        return Pair.of(client.createAuthUrl(principalId, effectiveState), effectiveState);
    }
}
