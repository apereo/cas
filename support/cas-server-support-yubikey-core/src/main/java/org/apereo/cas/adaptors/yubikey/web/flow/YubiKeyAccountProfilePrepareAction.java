package org.apereo.cas.adaptors.yubikey.web.flow;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;

/**
 * This is {@link YubiKeyAccountProfilePrepareAction}.
 * Decides whether the account profile offers to register a YubiKey: when multiple devices
 * may be registered, or when the user has no device yet, and device registration is enabled.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class YubiKeyAccountProfilePrepareAction extends ConsumerExecutionAction {
    /**
     * Flow scope attribute that tells the account profile whether YubiKey registration is offered.
     */
    public static final String FLOW_SCOPE_ATTR_REGISTRATION_ENABLED = "yubikeyAccountProfileRegistrationEnabled";

    public YubiKeyAccountProfilePrepareAction(final YubiKeyAccountRegistry registry,
                                              final CasConfigurationProperties casProperties) {
        super(requestContext -> {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val multipleDeviceRegistrationEnabled = casProperties.getAuthn().getMfa().getYubikey().isMultipleDeviceRegistrationEnabled();
            val enabled = (multipleDeviceRegistrationEnabled || !registry.isYubiKeyRegisteredFor(principal.getId()))
                && MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_REGISTRATION_ENABLED, enabled);
        });
    }
}
