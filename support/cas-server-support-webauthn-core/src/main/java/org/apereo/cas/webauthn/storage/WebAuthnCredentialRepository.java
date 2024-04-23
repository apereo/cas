package org.apereo.cas.webauthn.storage;

import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;

import java.util.stream.Stream;

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
