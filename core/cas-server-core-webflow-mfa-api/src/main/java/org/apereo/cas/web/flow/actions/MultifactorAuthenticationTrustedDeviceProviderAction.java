package org.apereo.cas.web.flow.actions;

import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link MultifactorAuthenticationTrustedDeviceProviderAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrustedDeviceProviderAction extends Action, Ordered, NamedObject {
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
