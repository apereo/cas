package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Objects;

/**
 * This is {@link GoogleAuthenticatorAuthorizeTokenAttemptAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticatorAuthorizeTokenAttemptAction extends BaseCasWebflowAction {
    private static final String FLOW_SCOPE_ATTEMPT_COUNTER = "GoogleAuthenticatorTokenAttemptCount";
    
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val maxAllowedAttempts = casProperties.getAuthn().getMfa().getGauth().getCore().getMaximumAuthenticationAttempts();
        val credential = Objects.requireNonNull(WebUtils.getCredential(requestContext));
        var attemptCounter = requestContext.getFlowScope().get(FLOW_SCOPE_ATTEMPT_COUNTER, Integer.class, 0);
        LOGGER.debug("Attempt counter for token [{}] is [{}]", credential.getId(), attemptCounter);
        if (maxAllowedAttempts <= 0 || attemptCounter < maxAllowedAttempts) {
            LOGGER.debug("Token [{}] is allowed to proceed with authentication", credential.getId());
            attemptCounter++;
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTEMPT_COUNTER, attemptCounter);
            return success(attemptCounter);
        }
        LOGGER.warn("Token [{}] has exceeded the maximum number of attempts [{}]", credential.getId(), attemptCounter);
        return error();
    }
}
