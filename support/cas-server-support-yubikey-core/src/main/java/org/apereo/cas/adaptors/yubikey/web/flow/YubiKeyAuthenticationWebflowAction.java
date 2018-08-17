package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class YubiKeyAuthenticationWebflowAction extends AbstractAction {
    private final CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.yubikeyAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
