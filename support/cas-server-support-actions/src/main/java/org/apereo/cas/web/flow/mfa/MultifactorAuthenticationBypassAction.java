package org.apereo.cas.web.flow.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Action that is responsible for determing if this MFA provider for the current subflow can
 * be bypassed for the user attempting to login into the service.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
public class MultifactorAuthenticationBypassAction extends AbstractMultifactorAuthenticationAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final RegisteredService service = WebUtils.getRegisteredService(requestContext);
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext();

        final MultifactorAuthenticationProviderBypass bypass = provider.getBypassEvaluator();

        // Transitioned here by another action to set the authentication bypass
        if (requestContext.getCurrentTransition().getId().equals(CasWebflowConstants.TRANSITION_ID_BYPASS)) {
            LOGGER.debug("Bypass triggered by MFA webflow for MFA for user [{}] for provider [{}]",
                    authentication.getPrincipal().getId(), provider.getId());
            bypass.updateAuthenticationToRememberBypass(authentication, provider);
            LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
                    authentication.getPrincipal().getId(), provider.getId());
            return yes();
        }

        if (bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request)) {
            LOGGER.debug("Bypass rules determined MFA should execute for user [{}] for provider [{}]",
                    authentication.getPrincipal().getId(), provider.getId());
            bypass.updateAuthenticationToForgetBypass(authentication);
            LOGGER.debug("Authentication updated to forget any existing bypass for user [{}] for provider [{}]",
                    authentication.getPrincipal().getId(), provider.getId());
            return no();
        }
        LOGGER.debug("Bypass rules determined MFA should NOT execute for user [{}] for provider [{}]",
                authentication.getPrincipal().getId(), provider.getId());
        bypass.updateAuthenticationToRememberBypass(authentication, provider);
        LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
                authentication.getPrincipal().getId(), provider.getId());
        return yes();
    }
}
