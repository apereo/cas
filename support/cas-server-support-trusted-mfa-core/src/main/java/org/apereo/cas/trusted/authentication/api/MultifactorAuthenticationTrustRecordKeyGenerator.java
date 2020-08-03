package org.apereo.cas.trusted.authentication.api;

/**
 * This is {@link MultifactorAuthenticationTrustRecordKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrustRecordKeyGenerator {
    String generate(MultifactorAuthenticationTrustRecord record);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
