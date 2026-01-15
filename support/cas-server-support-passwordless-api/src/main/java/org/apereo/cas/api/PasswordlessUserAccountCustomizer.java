package org.apereo.cas.api;

import module java.base;
import org.springframework.core.Ordered;

/**
 * This is {@link PasswordlessUserAccountCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface PasswordlessUserAccountCustomizer extends Ordered {
    /**
     * Customize.
     *
     * @param account the account
     * @return the optional
     */
    Optional<? extends PasswordlessUserAccount> customize(Optional<? extends PasswordlessUserAccount> account);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * No op passwordless user account customizer.
     *
     * @return the passwordless user account customizer
     */
    static PasswordlessUserAccountCustomizer noOp() {
        return account -> account;
    }
}
