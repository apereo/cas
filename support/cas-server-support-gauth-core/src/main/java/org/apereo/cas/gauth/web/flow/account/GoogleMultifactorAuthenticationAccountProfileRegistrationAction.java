package org.apereo.cas.gauth.web.flow.account;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GoogleMultifactorAuthenticationAccountProfileRegistrationAction extends ConsumerExecutionAction {
    public GoogleMultifactorAuthenticationAccountProfileRegistrationAction(final MultifactorAuthenticationProvider provider) {
        super(requestContext -> MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, provider));
    }
}

