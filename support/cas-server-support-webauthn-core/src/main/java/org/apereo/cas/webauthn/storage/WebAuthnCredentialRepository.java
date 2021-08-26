package org.apereo.cas.webauthn.storage;

import com.yubico.core.InMemoryRegistrationStorage;
import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.CredentialRepository;

import java.util.Set;
import java.util.stream.Stream;

/**
 * This is {@link WebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface WebAuthnCredentialRepository extends RegistrationStorage, CredentialRepository, WebAuthnRegistrationStorageCleaner {

    /**
     * In memory web authn credential repository.
     *
     * @return the web authn credential repository
     */
    static WebAuthnCredentialRepository inMemory() {
        return new InMemoryWebAuthn();
    }

    /**
     * Stream.
     *
     * @return the stream
     */
    Stream<? extends CredentialRegistration> stream();

    class InMemoryWebAuthn extends InMemoryRegistrationStorage implements WebAuthnCredentialRepository {
        @Override
        public Stream<? extends CredentialRegistration> stream() {
            return getStorage().asMap().values().stream().flatMap(Set::stream);
        }
    }
}
