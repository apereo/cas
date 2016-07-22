package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;
    private static final String SECRET_KEY = "iBIehjui12aK8x82oe5qzGeb0As=";
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @Before
    public void before() {
        final RequestContext ctx = mock(RequestContext.class);
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        WebUtils.putAuthentication(TestUtils.getAuthentication(), ctx);
        RequestContextHolder.setRequestContext(ctx);
    }
    
    @Test
    public void checkDefaultAccountRegistry() {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        assertNull(handler.getRegistry());
    }

    @Test(expected = FailedLoginException.class)
    public void checkReplayedAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        handler.authenticate(new YubiKeyCredential(OTP));
    }

    @Test(expected = AccountNotFoundException.class)
    public void checkBadConfigAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(123456, "123456");
        handler.authenticate(new YubiKeyCredential("casuser"));
    }

    @Test(expected = AccountNotFoundException.class)
    public void checkAccountNotFound() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        handler.setRegistry((uid, yubikeyPublicId) -> false);

        handler.authenticate(new YubiKeyCredential(OTP));
    }
}


