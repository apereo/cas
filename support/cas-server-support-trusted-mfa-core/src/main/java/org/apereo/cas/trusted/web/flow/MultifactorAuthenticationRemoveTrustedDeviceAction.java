package org.apereo.cas.trusted.web.flow;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationRemoveTrustedDeviceAction}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class MultifactorAuthenticationRemoveTrustedDeviceAction extends BaseCasWebflowAction {
    protected final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val key = requestContext.getRequestParameters().getRequired("key");
        mfaTrustEngine.remove(key);
        return success();
    }
}
