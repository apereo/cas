package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.registry.PermissiveYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.support.WebUtils;
import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.LinkedHashMap;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("MFAProvider")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;

    private static final String SECRET_KEY = EncodingUtils.encodeBase64("iBIehjui12aK8x82oe5qzGeb0As=");

    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void before() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
    }

    @Test
    void checkNoAuthN() {
        val handler = getHandler(YubicoClient.getClient(123456, EncodingUtils.encodeBase64("123456")));
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new YubiKeyCredential(OTP), mock(Service.class)));
    }

    private static YubiKeyAuthenticationHandler getHandler(final YubicoClient client) {
        return new YubiKeyAuthenticationHandler(client,
            new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class)));
    }

    @Test
    void checkDefaultAccountRegistry() {
        val handler = getHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));
        assertNotNull(handler.getRegistry());
    }

    @Test
    void checkSuccessAuthn() throws Throwable {
        val client = mock(YubicoClient.class);
        val response = mock(VerificationResponse.class);
        when(response.getStatus()).thenReturn(ResponseStatus.OK);
        when(client.verify(anyString())).thenReturn(response);
        val handler = getHandler(client);
        val result = handler.authenticate(new YubiKeyCredential(OTP), mock(Service.class));
        assertNotNull(result);
    }

    @Test
    void checkFailsVerificationAuthn() throws Throwable {
        val client = mock(YubicoClient.class);
        when(client.verify(anyString())).thenThrow(new YubicoVerificationException("fails"));
        val handler = getHandler(client);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new YubiKeyCredential(OTP), mock(Service.class)));
    }

    @Test
    void checkReplayedAuthn() {
        val handler = getHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new YubiKeyCredential(OTP), mock(Service.class)));
    }

    @Test
    void checkBadConfigAuthn() {
        val handler = getHandler(YubicoClient.getClient(123456, EncodingUtils.encodeBase64("123456")));
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(new YubiKeyCredential("casuser"), mock(Service.class)));
    }

    @Test
    void checkAccountNotFound() {
        val registry = new PermissiveYubiKeyAccountRegistry(new LinkedHashMap<>(),
            new DefaultYubiKeyAccountValidator(YubicoClient.getClient(CLIENT_ID, SECRET_KEY)));
        registry.setCipherExecutor(CipherExecutor.noOpOfSerializableToString());
        val handler = new YubiKeyAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(),
            YubicoClient.getClient(CLIENT_ID, SECRET_KEY),
            registry, null, new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class)));
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(new YubiKeyCredential(OTP), mock(Service.class)));
    }

    @Test
    void checkEncryptedAccount() {
        val registry = new PermissiveYubiKeyAccountRegistry(new LinkedHashMap<>(), (uid, token) -> true);
        assertNotNull(registry.save(YubiKeyAccount.builder().username(UUID.randomUUID().toString()).build()));
        val cipherExecutor = new YubikeyAccountCipherExecutor(
            "1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w", 0, 0);
        cipherExecutor.setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        registry.setCipherExecutor(cipherExecutor);

        val request = YubiKeyDeviceRegistrationRequest.builder().username("encrypteduser").token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(registry.registerAccountFor(request));
        assertTrue(registry.isYubiKeyRegisteredFor("encrypteduser", registry.getAccountValidator().getTokenPublicId(OTP)));
    }
}


