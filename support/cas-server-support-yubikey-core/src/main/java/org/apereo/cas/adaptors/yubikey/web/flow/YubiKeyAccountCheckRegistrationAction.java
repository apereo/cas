package org.apereo.cas.adaptors.yubikey.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class YubiKeyAccountCheckRegistrationAction extends AbstractAction {
    private final YubiKeyAccountRegistry registry;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        if (registry.isYubiKeyRegisteredFor(uid)) {
            return success();
        }
        return new EventFactorySupport().event(this, "register");
    }
}
