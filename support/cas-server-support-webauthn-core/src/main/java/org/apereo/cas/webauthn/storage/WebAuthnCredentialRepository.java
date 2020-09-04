package org.apereo.cas.webauthn.storage;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.core.InMemoryRegistrationStorage;
import com.yubico.webauthn.core.RegistrationStorage;

/**
 * This is {@link WebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface WebAuthnCredentialRepository extends RegistrationStorage, CredentialRepository, WebAuthnRegistrationStorageCleaner {

    static WebAuthnCredentialRepository inMemory() {
        return new InMemoryWebAuthn();
    }

    class InMemoryWebAuthn extends InMemoryRegistrationStorage implements WebAuthnCredentialRepository {
    }
}
