package org.apereo.cas.webauthn.web.flow.account;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

/**
 * This is {@link WebAuthnMultifactorAccountProfileRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WebAuthnMultifactorAccountProfileRegistrationAction extends ConsumerExecutionAction {
    public WebAuthnMultifactorAccountProfileRegistrationAction(final MultifactorAuthenticationProvider provider) {
        super(requestContext -> MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, provider));
    }
}

