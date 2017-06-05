package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.YubicoClient;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.registry.WhitelistYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;
    private static final String SECRET_KEY = "iBIehjui12aK8x82oe5qzGeb0As=";
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        final RequestContext ctx = mock(RequestContext.class);
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), ctx);
        RequestContextHolder.setRequestContext(ctx);
    }

    @Test
    public void checkDefaultAccountRegistry() {
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));
        assertNotNull(handler.getRegistry());
    }

    @Test
    public void checkReplayedAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(CLIENT_ID, SECRET_KEY));

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Authentication failed with status: REPLAYED_OTP");

        handler.authenticate(new YubiKeyCredential(OTP));
    }

    @Test
    public void checkBadConfigAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(YubicoClient.getClient(123456, "123456"));

        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("OTP format is invalid");

        handler.authenticate(new YubiKeyCredential("casuser"));
    }

    @Test
    public void checkAccountNotFound() throws Exception {
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(StringUtils.EMPTY,
                null, new DefaultPrincipalFactory(),
                YubicoClient.getClient(CLIENT_ID, SECRET_KEY),
                new WhitelistYubiKeyAccountRegistry(new HashMap<>(),
                        new DefaultYubiKeyAccountValidator(YubicoClient.getClient(CLIENT_ID, SECRET_KEY))));
        this.thrown.expect(AccountNotFoundException.class);
        handler.authenticate(new YubiKeyCredential(OTP));
    }
}


