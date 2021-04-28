package org.apereo.cas.authentication;

/**
 * This is {@link MultifactorAuthenticationHandler}.
 * It represents the common operations that a given handler
 * might expose or implement for multifactor authentication,
 * or can be treated as a marker interface.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationHandler extends AuthenticationHandler {
}
