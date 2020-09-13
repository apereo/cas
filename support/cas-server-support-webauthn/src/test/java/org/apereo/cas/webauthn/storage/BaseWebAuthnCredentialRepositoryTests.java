package org.apereo.cas.webauthn.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;

import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.UserIdentity;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.relying-party-id=example.org"
    })
public abstract class BaseWebAuthnCredentialRepositoryTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    protected WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val registration = getCredentialRegistration(id);

        assertTrue(webAuthnCredentialRepository.addRegistrationByUsername(id, registration));
        assertFalse(webAuthnCredentialRepository.getCredentialIdsForUsername(id).isEmpty());

        val ba = ByteArray.fromBase64Url(id);
        assertTrue(webAuthnCredentialRepository.getRegistrationByUsernameAndCredentialId(id, ba).isPresent());
        assertFalse(webAuthnCredentialRepository.getRegistrationsByUserHandle(ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.getRegistrationsByUsername(id).isEmpty());
        assertFalse(webAuthnCredentialRepository.getUserHandleForUsername(id).isEmpty());
        assertFalse(webAuthnCredentialRepository.getUsernameForUserHandle(ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.lookup(ba, ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.lookupAll(ba).isEmpty());

        val constructor = AssertionResult.class.getDeclaredConstructor(boolean.class, ByteArray.class,
            ByteArray.class, String.class, long.class, boolean.class, List.class);
        constructor.setAccessible(true);
        val result = constructor.newInstance(true, ba, ba, id, 1, true, List.of());
        webAuthnCredentialRepository.updateSignatureCount(result);

        webAuthnCredentialRepository.removeAllRegistrations(id);
        webAuthnCredentialRepository.removeRegistrationByUsername(id, registration);
        assertTrue(webAuthnCredentialRepository.lookup(ba, ba).isEmpty());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                webAuthnCredentialRepository.clean();
            }
        });
    }

    @SneakyThrows
    protected static CredentialRegistration getCredentialRegistration(final String username) {
        return CredentialRegistration.builder()
            .registrationTime(Instant.now(Clock.systemUTC()))
            .credential(RegisteredCredential.builder()
                .credentialId(ByteArray.fromBase64Url(username))
                .userHandle(ByteArray.fromBase64Url(username))
                .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .build())
            .userIdentity(UserIdentity.builder()
                .name(username)
                .displayName("CAS")
                .id(ByteArray.fromBase64Url(username))
                .build())
            .build();
    }


}
