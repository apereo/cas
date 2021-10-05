package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.registry.PermissiveYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.webflow.execution.RequestContextHolder.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("MFAProvider")
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;

    private static final String SECRET_KEY = EncodingUtils.encodeBase64("iBIehjui12aK8x82oe5qzGeb0As=");

    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @BeforeEach
    public void before() {
        val ctx = mock(RequestContext.class);
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), ctx);
        setRequestContext(ctx);
    }

    @Test
    public void checkNoAuthN() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(123456, EncodingUtils.encodeBase64("123456")));
        assertThrows(IllegalArgumentException.class, () -> handler.authenticate(new YubiKeyCredential(OTP)));
    }

    @Test
    public void checkDefaultAccountRegistry() {
        val handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));
        assertNotNull(handler.getRegistry());
    }

    @Test
    public void checkSuccessAuthn() throws Exception {
        val client = mock(YubicoClient.class);
        val response = mock(VerificationResponse.class);
        when(response.getStatus()).thenReturn(ResponseStatus.OK);
        when(client.verify(anyString())).thenReturn(response);
        val handler = new YubiKeyAuthenticationHandler(client);
        val result = handler.authenticate(new YubiKeyCredential(OTP));
        assertNotNull(result);
    }

    @Test
    public void checkFailsVerificationAuthn() throws Exception {
        val client = mock(YubicoClient.class);
        when(client.verify(anyString())).thenThrow(new YubicoVerificationException("fails"));
        val handler = new YubiKeyAuthenticationHandler(client);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new YubiKeyCredential(OTP)));
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
        val registry = new PermissiveYubiKeyAccountRegistry(new LinkedHashMap<>(),
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
        val registry = new PermissiveYubiKeyAccountRegistry(new LinkedHashMap<>(), (uid, token) -> true);
        assertNotNull(registry.save(YubiKeyAccount.builder().username(UUID.randomUUID().toString()).build()));
        registry.setCipherExecutor(new YubikeyAccountCipherExecutor(
            "1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w", 0, 0));

        val request = YubiKeyDeviceRegistrationRequest.builder().username("encrypteduser").token(OTP).name(UUID.randomUUID().toString()).build();
        assertTrue(registry.registerAccountFor(request));
        assertTrue(registry.isYubiKeyRegisteredFor("encrypteduser", registry.getAccountValidator().getTokenPublicId(OTP)));
    }
}


