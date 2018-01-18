package org.apereo.cas.adaptors.yubikey.web.flow;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
