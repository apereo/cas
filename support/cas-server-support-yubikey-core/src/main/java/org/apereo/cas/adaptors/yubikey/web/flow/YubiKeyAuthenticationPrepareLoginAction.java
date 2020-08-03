package org.apereo.cas.adaptors.yubikey.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class YubiKeyAuthenticationPrepareLoginAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        WebUtils.putYubiKeyMultipleDeviceRegistrationEnabled(requestContext,
            casProperties.getAuthn().getMfa().getYubikey().isMultipleDeviceRegistrationEnabled());
        return null;
    }
}
