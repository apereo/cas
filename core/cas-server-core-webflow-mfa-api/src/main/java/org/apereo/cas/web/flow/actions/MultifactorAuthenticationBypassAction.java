package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that is responsible for determining if this MFA provider for the current subflow can
 * be bypassed for the user attempting to login into the service.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@RequiredArgsConstructor
public class MultifactorAuthenticationBypassAction extends AbstractMultifactorAuthenticationAction {
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();

        val bypass = provider.getBypassEvaluator();
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        val bypassAllowed = isMultifactorAuthenticationBypass(requestContext, registeredService, provider);
        if (bypassAllowed) {
            LOGGER.debug("Bypass triggered by MFA webflow for MFA for user [{}] for provider [{}]",
                principal.getId(), provider.getId());
            bypass.rememberBypass(authentication, provider);
            LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
                principal.getId(), provider.getId());
            return yes();
        }

        if (bypass.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request, service)) {
            LOGGER.debug("Bypass rules determined MFA should execute for user [{}] and provider [{}]",
                principal.getId(), provider.getId());
            bypass.forgetBypass(authentication);
            LOGGER.debug("Authentication updated to forget any existing bypass for user [{}] for provider [{}]",
                principal.getId(), provider.getId());
            return no();
        }
        LOGGER.debug("Bypass rules determined MFA should NOT execute for user [{}] for provider [{}]",
            principal.getId(), provider.getId());
        bypass.rememberBypass(authentication, provider);
        LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
            principal.getId(), provider.getId());
        return yes();
    }

    protected boolean isMultifactorAuthenticationBypass(final RequestContext requestContext,
                                                        final RegisteredService service,
                                                        final MultifactorAuthenticationProvider provider) {
        val failureEval = provider.getFailureModeEvaluator();
        return requestContext.getCurrentTransition().getId().equals(CasWebflowConstants.TRANSITION_ID_BYPASS)
               || (failureEval != null && failureEval.evaluate(service, provider).isAllowedToBypass() && !provider.isAvailable(service));
    }
}
