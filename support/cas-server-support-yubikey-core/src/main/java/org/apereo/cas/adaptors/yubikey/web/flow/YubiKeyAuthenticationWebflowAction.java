package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    public YubiKeyAuthenticationWebflowAction(final CasWebflowEventResolver casWebflowEventResolver) {
        this.yubikeyAuthenticationWebflowEventResolver = casWebflowEventResolver;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.yubikeyAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
