package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.scope.ConversationScope;
import org.springframework.webflow.scope.FlashScope;
import org.springframework.webflow.scope.FlowScope;
import org.springframework.webflow.scope.RequestScope;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link DuoSecurityUniversalPromptPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DuoSecurityUniversalPromptValidateLoginAction extends DuoSecurityAuthenticationWebflowAction {
    static final String REQUEST_PARAMETER_CODE = "duo_code";

    static final String REQUEST_PARAMETER_STATE = "state";

    private final BrowserWebStorageSessionStore sessionStore;

    private final ConfigurableApplicationContext applicationContext;

    public DuoSecurityUniversalPromptValidateLoginAction(
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        final BrowserWebStorageSessionStore sessionStore,
        final ConfigurableApplicationContext applicationContext) {
        super(duoAuthenticationWebflowEventResolver);
        this.sessionStore = sessionStore;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val requestParameters = requestContext.getRequestParameters();
        if (requestParameters.contains(REQUEST_PARAMETER_CODE) && requestParameters.contains(REQUEST_PARAMETER_STATE)) {
            return handleDuoSecurityUniversalPromptResponse(requestContext);
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
    }

    private Event handleDuoSecurityUniversalPromptResponse(final RequestContext requestContext) throws Exception {
        val browserStorage = WebUtils.getBrowserStoragePayload(requestContext);
        if (browserStorage.isEmpty()) {
            WebUtils.putTargetTransition(requestContext, CasWebflowConstants.TRANSITION_ID_SWITCH);
            WebUtils.putTargetState(requestContext, requestContext.getCurrentState().getId());
            WebUtils.putBrowserStorageContextKey(requestContext, sessionStore.getBrowserStorageContextKey());
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_RESTORE);
        }

        val duoState = WebUtils.getRequestParameterOrAttribute(requestContext, REQUEST_PARAMETER_STATE).orElseThrow();
        LOGGER.trace("Received Duo Security state [{}]", duoState);
        BrowserWebStorageSessionStore browserSessionStore = null;

        val webContext = toWebContext(requestContext);
        try {
            browserSessionStore = sessionStore
                .buildFromTrackableSession(webContext, browserStorage.get())
                .map(BrowserWebStorageSessionStore.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unable to determine Duo authentication context from session store"));

            browserSessionStore.getSessionAttributes(webContext).forEach((key, value) -> {
                if (key.equalsIgnoreCase(FlowScope.class.getSimpleName())) {
                    populateRequestContextScope(value, requestContext.getFlowScope());
                } else if (key.equalsIgnoreCase(FlashScope.class.getSimpleName())) {
                    populateRequestContextScope(value, requestContext.getFlashScope());
                } else if (key.equalsIgnoreCase(RequestScope.class.getSimpleName())) {
                    populateRequestContextScope(value, requestContext.getRequestScope());
                } else if (key.equalsIgnoreCase(ConversationScope.class.getSimpleName())) {
                    populateRequestContextScope(value, requestContext.getConversationScope());
                } else {
                    requestContext.getFlowScope().put(key, value);
                }
            });
            populateContextWithCredential(requestContext, browserSessionStore);
            populateContextWithAuthentication(requestContext, browserSessionStore);
            populateContextWithService(requestContext, browserSessionStore);
            return super.doExecuteInternal(requestContext);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        } finally {
            if (browserSessionStore != null) {
                val credential = (Credential) browserSessionStore.getSessionAttributes(webContext).get(Credential.class.getSimpleName());
                WebUtils.putCredential(requestContext, credential);
            }
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
    }

    private static JEEContext toWebContext(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        return new JEEContext(request, response);
    }

    private static void populateRequestContextScope(final Object flowAttributes, final MutableAttributeMap<Object> requestContext) {
        val mappedAttributes = new LinkedHashMap<>((Map) flowAttributes);
        CollectionUtils.filter(mappedAttributes.values(), PredicateUtils.notNullPredicate());
        requestContext.putAll(new LocalAttributeMap<>(mappedAttributes));
    }

    protected void populateContextWithService(final RequestContext requestContext,
                                              final BrowserWebStorageSessionStore sessionStorage) {
        val webContext = toWebContext(requestContext);
        val registeredService = (RegisteredService) sessionStorage.getSessionAttributes(webContext).get(RegisteredService.class.getSimpleName());
        WebUtils.putRegisteredService(requestContext, registeredService);
        LOGGER.debug("Restored registered service [{}] into webflow context", registeredService);
        val service = (Service) sessionStorage.getSessionAttributes(webContext).get(Service.class.getSimpleName());
        WebUtils.putServiceIntoFlowScope(requestContext, service);
        LOGGER.debug("Restored service [{}] into webflow context", service);
    }

    protected void populateContextWithCredential(final RequestContext requestContext,
                                                 final BrowserWebStorageSessionStore sessionStorage) {
        val webContext = toWebContext(requestContext);
        val authentication = (Authentication) sessionStorage.getSessionAttributes(webContext).get(Authentication.class.getSimpleName());
        val duoCode = WebUtils.getRequestParameterOrAttribute(requestContext, REQUEST_PARAMETER_CODE).orElseThrow();
        LOGGER.trace("Received Duo Security code [{}]", duoCode);
        val duoSecurityIdentifier = (String) sessionStorage.getSessionAttributes(webContext).get("duoProviderId");
        val credential = new DuoSecurityUniversalPromptCredential(duoCode, authentication);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(duoSecurityIdentifier, applicationContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to locate multifactor authentication provider by id " + duoSecurityIdentifier));
        credential.setProviderId(provider.getId());
        WebUtils.putCredential(requestContext, credential);
    }

    protected void populateContextWithAuthentication(final RequestContext requestContext,
                                                     final BrowserWebStorageSessionStore sessionStorage) throws Throwable {
        val webContext = toWebContext(requestContext);
        val authenticationResultBuilder = (AuthenticationResultBuilder) sessionStorage.getSessionAttributes(webContext)
            .get(AuthenticationResultBuilder.class.getSimpleName());
        FunctionUtils.doIfNotNull(authenticationResultBuilder, value -> WebUtils.putAuthenticationResultBuilder(value, requestContext));
        val authenticationResult = authenticationResultBuilder.build();
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        Objects.requireNonNull(authenticationResult.getAuthentication());
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
    }
}
