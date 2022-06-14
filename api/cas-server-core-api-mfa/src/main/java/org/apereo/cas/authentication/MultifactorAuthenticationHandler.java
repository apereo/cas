package org.apereo.cas.authentication;

import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link MultifactorAuthenticationHandler}.
 * It represents the common operations that a given handler
 * might expose or implement for multifactor authentication,
 * or can be treated as a marker interface.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface MultifactorAuthenticationHandler extends AuthenticationHandler {
    /**
     * Gets multifactor provider id linked to this handler.
     *
     * @return the multifactor provider id
     */
    ObjectProvider<? extends MultifactorAuthenticationProvider> getMultifactorAuthenticationProvider();
}
