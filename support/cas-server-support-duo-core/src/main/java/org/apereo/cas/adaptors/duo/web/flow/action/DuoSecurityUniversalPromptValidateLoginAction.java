package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BrowserSessionStorage;
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
import org.springframework.webflow.core.collection.ParameterMap;
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

    private final AuthenticationSystemSupport authenticationSystemSupport;

    public DuoSecurityUniversalPromptValidateLoginAction(
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        final BrowserWebStorageSessionStore sessionStore,
        final ConfigurableApplicationContext applicationContext,
        final AuthenticationSystemSupport authenticationSystemSupport) {
        super(duoAuthenticationWebflowEventResolver);
        this.sessionStore = sessionStore;
        this.applicationContext = applicationContext;
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val requestParameters = requestContext.getRequestParameters();
        if (requestParameters.contains(REQUEST_PARAMETER_CODE) && requestParameters.contains(REQUEST_PARAMETER_STATE)) {
            return handleDuoSecurityUniversalPromptResponse(requestContext, requestParameters);
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
    }

    private Event handleDuoSecurityUniversalPromptResponse(final RequestContext requestContext,
                                                           final ParameterMap requestParameters) {
        if (!requestParameters.contains(BrowserSessionStorage.KEY_SESSION_STORAGE)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_RESTORE);
        }

        val duoState = requestParameters.get(REQUEST_PARAMETER_STATE, String.class);
        LOGGER.trace("Received Duo Security state [{}]", duoState);
        BrowserWebStorageSessionStore browserSessionStore = null;

        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

            val storage = requestParameters.get(BrowserSessionStorage.KEY_SESSION_STORAGE);
            val context = new JEEContext(request, response);
            browserSessionStore = this.sessionStore
                .buildFromTrackableSession(context, storage)
                .map(BrowserWebStorageSessionStore.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unable to determine Duo authentication context from session store"));

            browserSessionStore.getSessionAttributes().forEach((key, value) -> {
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
            val authentication = (Authentication) browserSessionStore.getSessionAttributes().get(Authentication.class.getSimpleName());
            populateContextWithCredential(requestContext, browserSessionStore, authentication);
            populateContextWithAuthentication(requestContext, browserSessionStore);
            populateContextWithService(requestContext, browserSessionStore);
            return super.doExecuteInternal(requestContext);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        } finally {
            if (browserSessionStore != null) {
                val credential = (Credential) browserSessionStore.getSessionAttributes().get(Credential.class.getSimpleName());
                WebUtils.putCredential(requestContext, credential);
            }
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
    }

    private static void populateRequestContextScope(final Object flowAttributes, final MutableAttributeMap<Object> requestContext) {
        val mappedAttributes = new LinkedHashMap<>((Map) flowAttributes);
        CollectionUtils.filter(mappedAttributes.values(), PredicateUtils.notNullPredicate());
        requestContext.putAll(new LocalAttributeMap<>(mappedAttributes));
    }

    protected void populateContextWithService(final RequestContext requestContext,
                                              final BrowserWebStorageSessionStore sessionStorage) {
        val registeredService = (RegisteredService) sessionStorage.getSessionAttributes().get(RegisteredService.class.getSimpleName());
        WebUtils.putRegisteredService(requestContext, registeredService);
        val service = (Service) sessionStorage.getSessionAttributes().get(Service.class.getSimpleName());
        WebUtils.putServiceIntoFlowScope(requestContext, service);
    }


    protected void populateContextWithCredential(final RequestContext requestContext,
                                                 final BrowserWebStorageSessionStore sessionStorage,
                                                 final Authentication authentication) {
        val requestParameters = requestContext.getRequestParameters();
        val duoCode = requestParameters.get(REQUEST_PARAMETER_CODE, String.class);
        LOGGER.trace("Received Duo Security code [{}]", duoCode);

        val duoSecurityIdentifier = (String) sessionStorage.getSessionAttributes().get("duoProviderId");
        val credential = new DuoSecurityUniversalPromptCredential(duoCode, authentication);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(duoSecurityIdentifier, applicationContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to locate multifactor authentication provider by id " + duoSecurityIdentifier));
        credential.setProviderId(provider.getId());
        WebUtils.putCredential(requestContext, credential);
    }

    protected void populateContextWithAuthentication(final RequestContext requestContext, final BrowserWebStorageSessionStore sessionStorage) throws Throwable {
        val authenticationResultBuilder = (AuthenticationResultBuilder) sessionStorage.getSessionAttributes().get(AuthenticationResultBuilder.class.getSimpleName());
        FunctionUtils.doIfNotNull(authenticationResultBuilder, value -> WebUtils.putAuthenticationResultBuilder(value, requestContext));
        val authenticationResult = authenticationResultBuilder.build(authenticationSystemSupport.getPrincipalElectionStrategy());
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        Objects.requireNonNull(authenticationResult.getAuthentication());
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
    }
}
