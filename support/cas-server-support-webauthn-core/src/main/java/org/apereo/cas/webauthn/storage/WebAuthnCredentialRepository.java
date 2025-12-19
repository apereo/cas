package org.apereo.cas.webauthn.storage;

import module java.base;
import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;

/**
 * This is {@link WebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface WebAuthnCredentialRepository extends RegistrationStorage, WebAuthnRegistrationStorageCleaner {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "webAuthnCredentialRepository";

    /**
     * Stream.
     *
     * @return the stream
     */
    Stream<? extends CredentialRegistration> stream();
}
