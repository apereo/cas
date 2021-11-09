package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class WebAuthnAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver authenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val result = this.authenticationWebflowEventResolver.resolveSingle(requestContext);
        if (!result.getId().equals(CasWebflowConstants.STATE_ID_SUCCESS)) {
            WebUtils.addErrorMessageToContext(requestContext, "cas.mfa.webauthn.auth.fail");
        }
        return result;
    }
}
