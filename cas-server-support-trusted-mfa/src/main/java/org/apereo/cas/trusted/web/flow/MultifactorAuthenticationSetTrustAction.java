package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationSetTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MultifactorAuthenticationSetTrustAction extends AbstractAction {
    private MultifactorAuthenticationTrustStorage storage;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return null;
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }
}
