package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import com.duosecurity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.scope.FlowScope;

import java.util.LinkedHashMap;
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
    private final CipherExecutor webflowCipherExecutor;

    private final MultifactorAuthenticationProviderBean<
        DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getInProgressAuthentication();
        val duoSecurityIdentifier = WebUtils.getMultifactorAuthenticationProviderById(requestContext);
        val duoProvider = duoProviderBean.getProvider(duoSecurityIdentifier);

        val client = duoProvider.getDuoAuthenticationService()
            .getDuoClient()
            .map(dc -> (Client) dc)
            .orElseThrow(() -> new RuntimeException("Unable to locate Duo Security client"));
        val state = client.generateState();
        val service = WebUtils.getService(requestContext);

        val properties = new LinkedHashMap<String, Object>();
        properties.put("duoProviderId", duoSecurityIdentifier);
        properties.put(Authentication.class.getSimpleName(), authentication);
        properties.put(AuthenticationResultBuilder.class.getSimpleName(), WebUtils.getAuthenticationResultBuilder(requestContext));
        properties.put(AuthenticationResult.class.getSimpleName(), WebUtils.getAuthenticationResult(requestContext));
        properties.put(Credential.class.getSimpleName(), WebUtils.getMultifactorAuthenticationParentCredential(requestContext));
        FunctionUtils.doIfNotNull(service, __ -> properties.put(Service.class.getSimpleName(), service));
        properties.put(DuoSecurityAuthenticationService.class.getSimpleName(), state);

        val flowScope = requestContext.getFlowScope().asMap();
        properties.put(FlowScope.class.getSimpleName(), flowScope);

        Optional.ofNullable(WebUtils.getRegisteredService(requestContext))
            .ifPresent(registeredService -> properties.put(RegisteredService.class.getSimpleName(), registeredService));

        val principal = resolvePrincipal(authentication.getPrincipal());
        val authUrl = client.createAuthUrl(principal.getId(), state);

        requestContext.getFlowScope().put("duoUniversalPromptLoginUrl", authUrl);

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = new JEEContext(request, response);
        val sessionStorage = new BrowserWebStorageSessionStore(webflowCipherExecutor)
            .setSessionAttributes(properties)
            .getTrackableSession(context)
            .map(BrowserSessionStorage.class::cast)
            .orElseThrow(() -> new IllegalStateException("Unable to determine trackable session for storage"));
        sessionStorage.setDestinationUrl(authUrl);
        requestContext.getFlowScope().put(BrowserSessionStorage.KEY_SESSION_STORAGE, sessionStorage);

        LOGGER.debug("Redirecting to Duo Security url at [{}]", authUrl);
        return success(sessionStorage);
    }
}
