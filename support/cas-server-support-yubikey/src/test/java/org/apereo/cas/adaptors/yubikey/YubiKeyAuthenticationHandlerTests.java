package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.registry.WhitelistYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.client.v2.YubicoClient;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("MFA")
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;
    private static final String SECRET_KEY = EncodingUtils.encodeBase64("iBIehjui12aK8x82oe5qzGeb0As=");
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @BeforeEach
    public void before() {
        val ctx = mock(RequestContext.class);
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), ctx);
        RequestContextHolder.setRequestContext(ctx);
    }

    @Test
    public void checkDefaultAccountRegistry() {
        val handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));
        assertNotNull(handler.getRegistry());
    }

    @Test
    public void checkReplayedAuthn() {
        val handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));

        assertThrows(FailedLoginException.class, () -> handler.authenticate(new YubiKeyCredential(OTP)));
    }

    @Test
    public void checkBadConfigAuthn() {
        val handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(123456, EncodingUtils.encodeBase64("123456")));
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(new YubiKeyCredential("casuser")));
    }

    @Test
    public void checkAccountNotFound() {
        val registry = new WhitelistYubiKeyAccountRegistry(new LinkedMultiValueMap<>(),
            new DefaultYubiKeyAccountValidator(YubicoClient.getClient(CLIENT_ID, SECRET_KEY)));
        registry.setCipherExecutor(CipherExecutor.noOpOfSerializableToString());
        val handler = new YubiKeyAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(),
            YubicoClient.getClient(CLIENT_ID, SECRET_KEY),
            registry, null);
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(new YubiKeyCredential(OTP)));
    }

    @Test
    public void checkEncryptedAccount() {
        val registry = new WhitelistYubiKeyAccountRegistry(new LinkedMultiValueMap<>(), (uid, token) -> true);
        registry.setCipherExecutor(new YubikeyAccountCipherExecutor(
            "1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w", 0, 0));
        assertTrue(registry.registerAccountFor("encrypteduser", OTP));
        assertTrue(registry.isYubiKeyRegisteredFor("encrypteduser", registry.getAccountValidator().getTokenPublicId(OTP)));
    }
}


