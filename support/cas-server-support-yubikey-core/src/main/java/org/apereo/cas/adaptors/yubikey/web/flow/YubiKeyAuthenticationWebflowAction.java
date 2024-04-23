package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class YubiKeyAuthenticationWebflowAction extends BaseCasWebflowAction {
    private final CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> yubikeyAuthenticationWebflowEventResolver.resolveSingle(requestContext));
    }
}
