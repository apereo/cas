package org.apereo.cas.adaptors.yubikey.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class YubiKeyAuthenticationPrepareLoginAction extends BaseCasWebflowAction {
    protected final CasConfigurationProperties casProperties;
    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val registrationEnabled = casProperties.getAuthn().getMfa().getYubikey().isMultipleDeviceRegistrationEnabled()
            && MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
        MultifactorAuthenticationWebflowUtils.putYubiKeyMultipleDeviceRegistrationEnabled(requestContext, registrationEnabled);
        return null;
    }
}
