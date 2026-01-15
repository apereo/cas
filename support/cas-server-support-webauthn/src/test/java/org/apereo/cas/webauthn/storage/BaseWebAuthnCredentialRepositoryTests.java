package org.apereo.cas.webauthn.storage;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAttestationTrustSourceFidoProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.core.trust-source.fido.legal-header=" + WebAuthnMultifactorAttestationTrustSourceFidoProperties.DEFAULT_LEGAL_HEADER,
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org"
    })
@ExtendWith(CasTestExtension.class)
public abstract class BaseWebAuthnCredentialRepositoryTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    protected WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
    protected CipherExecutor<String, String> cipherExecutor;
    public static CredentialRegistration getCredentialRegistration(final String username) throws Exception {
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

    @Test
    protected void verifyOperation() throws Throwable {
        val id = getUsername();
        val registration = getCredentialRegistration(id.toLowerCase(Locale.ENGLISH));

        assertTrue(webAuthnCredentialRepository.addRegistrationByUsername(id.toLowerCase(Locale.ENGLISH), registration));
        assertFalse(webAuthnCredentialRepository.getCredentialIdsForUsername(id.toUpperCase(Locale.ENGLISH)).isEmpty());

        val ba = ByteArray.fromBase64Url(id);
        val newRegistration = webAuthnCredentialRepository.getRegistrationByUsernameAndCredentialId(id.toUpperCase(Locale.ENGLISH), ba);
        assertTrue(newRegistration.isPresent());
        assertNotNull(newRegistration.get().getRegistrationTime());
        assertFalse(webAuthnCredentialRepository.getRegistrationsByUserHandle(ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.getRegistrationsByUsername(id.toUpperCase(Locale.ENGLISH)).isEmpty());
        assertFalse(webAuthnCredentialRepository.getUserHandleForUsername(id.toUpperCase(Locale.ENGLISH)).isEmpty());
        assertFalse(webAuthnCredentialRepository.getUsernameForUserHandle(ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.lookup(ba, ba).isEmpty());
        assertFalse(webAuthnCredentialRepository.lookupAll(ba).isEmpty());
        assertTrue(webAuthnCredentialRepository.stream().findAny().isPresent());

        val credential = RegisteredCredential.builder()
            .credentialId(ba)
            .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .build();

        val result = mock(AssertionResult.class);
        when(result.getCredential()).thenReturn(credential);
        when(result.getSignatureCount()).thenReturn(1L);
        when(result.getUsername()).thenReturn(id);
        when(result.getCredentialId()).thenReturn(ba);

        webAuthnCredentialRepository.updateSignatureCount(result);

        webAuthnCredentialRepository.removeAllRegistrations(id.toUpperCase(Locale.ENGLISH));
        webAuthnCredentialRepository.removeRegistrationByUsername(id.toUpperCase(Locale.ENGLISH), registration);
        assertTrue(webAuthnCredentialRepository.lookup(ba, ba).isEmpty());

        assertDoesNotThrow(() -> webAuthnCredentialRepository.clean());
    }

    protected String getUsername() throws Exception {
        return UUID.randomUUID().toString();
    }
}
