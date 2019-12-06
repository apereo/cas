package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
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
public class MultifactorAuthenticationBypassAction extends AbstractMultifactorAuthenticationAction {

    public MultifactorAuthenticationBypassAction(final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val service = WebUtils.getRegisteredService(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();

        val bypass = provider.getBypassEvaluator();

        val principal = authentication.getPrincipal();
        if (requestContext.getCurrentTransition().getId().equals(CasWebflowConstants.TRANSITION_ID_BYPASS)) {
            LOGGER.debug("Bypass triggered by MFA webflow for MFA for user [{}] for provider [{}]",
                    principal.getId(), provider.getId());
            bypass.rememberBypass(authentication, provider);
            LOGGER.debug("Authentication updated to remember bypass for user [{}] for provider [{}]",
                    principal.getId(), provider.getId());
            return yes();
        }

        if (bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request)) {
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
}
