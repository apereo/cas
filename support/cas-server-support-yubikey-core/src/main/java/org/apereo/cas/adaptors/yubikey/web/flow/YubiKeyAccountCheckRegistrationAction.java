package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class YubiKeyAccountCheckRegistrationAction extends AbstractMultifactorAuthenticationAction<YubiKeyMultifactorAuthenticationProvider> {
    private final YubiKeyAccountRegistry registry;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val uid = resolvePrincipal(authentication.getPrincipal(), requestContext).getId();
        if (registry.isYubiKeyRegisteredFor(uid)) {
            return success();
        }
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
