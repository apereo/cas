package org.jasig.cas.adaptors.yubikey.web.flow;

import org.jasig.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("yubikeyAuthenticationWebflowAction")
public class YubiKeyAuthenticationWebflowAction extends AbstractAction {
    @Autowired
    @Qualifier("yubikeyAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return yubikeyAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
