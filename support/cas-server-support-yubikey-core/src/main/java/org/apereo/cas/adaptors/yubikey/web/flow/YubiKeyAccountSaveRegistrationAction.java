package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class YubiKeyAccountSaveRegistrationAction extends AbstractAction {
    private final YubiKeyAccountRegistry registry;

    public YubiKeyAccountSaveRegistrationAction(final YubiKeyAccountRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return null;
    }
}
