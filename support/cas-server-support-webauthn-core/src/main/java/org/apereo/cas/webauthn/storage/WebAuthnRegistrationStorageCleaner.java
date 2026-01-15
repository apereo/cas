package org.apereo.cas.webauthn.storage;
import module java.base;

/**
 * This is {@link WebAuthnRegistrationStorageCleaner}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface WebAuthnRegistrationStorageCleaner {

    /**
     * Clean.
     */
    default void clean() {
    }
}
