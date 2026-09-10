package org.apereo.cas.webauthn.storage;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.yubico.core.InMemoryRegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
class InMemoryWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {

    @Test
    void verifyCredentialLookupsReuseIdentifierIndex() throws Exception {
        val repository = new CountingInMemoryRegistrationStorage(casProperties, cipherExecutor);
        val username = UUID.randomUUID().toString();
        val registration = getCredentialRegistration(username);
        val credentialId = registration.getCredential().getCredentialId();
        val userHandle = registration.getCredential().getUserHandle();
        assertTrue(repository.addRegistrationByUsername(username, registration));

        assertFalse(repository.lookupAll(credentialId).isEmpty());
        assertTrue(repository.lookupAll(ByteArray.fromBase64Url(UUID.randomUUID().toString())).isEmpty());
        assertTrue(repository.lookup(credentialId, userHandle).isPresent());
        assertFalse(repository.getRegistrationsByUserHandle(userHandle).isEmpty());
        assertEquals(1, repository.streamCount.get());

        val secondUsername = UUID.randomUUID().toString();
        assertTrue(repository.addRegistrationByUsername(secondUsername, getCredentialRegistration(secondUsername)));
        assertFalse(repository.lookupAll(credentialId).isEmpty());
        assertEquals(2, repository.streamCount.get());
    }

    private static final class CountingInMemoryRegistrationStorage extends InMemoryRegistrationStorage {
        private final AtomicInteger streamCount = new AtomicInteger();

        private CountingInMemoryRegistrationStorage(final CasConfigurationProperties properties,
                                                    final CipherExecutor<String, String> cipherExecutor) {
            super(properties, cipherExecutor);
        }

        @Override
        public Stream<? extends CredentialRegistration> stream() {
            streamCount.incrementAndGet();
            return super.stream();
        }
    }
}
