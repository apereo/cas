package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BasePasswordlessCasWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BasePasswordlessCasWebflowAction extends BaseCasWebflowAction {

    protected final CasConfigurationProperties casProperties;


    /**
     * Should activate multifactor authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean shouldActivateMultifactorAuthenticationFor(final RequestContext requestContext,
                                                                 final PasswordlessUserAccount user) {
        val status = user.getMultifactorAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for multifactor authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for multifactor authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isMultifactorAuthenticationActivated();

    }
    
    /**
     * Should delegate authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean isDelegatedAuthenticationActiveFor(final RequestContext requestContext,
                                                         final PasswordlessUserAccount user) {
        val status = user.getDelegatedAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for delegated authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for delegated authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isDelegatedAuthenticationActivated();
    }
}
