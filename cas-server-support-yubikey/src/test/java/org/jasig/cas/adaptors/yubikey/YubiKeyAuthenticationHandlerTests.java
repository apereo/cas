package org.jasig.cas.adaptors.yubikey;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link YubiKeyAuthenticationHandler}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class YubiKeyAuthenticationHandlerTests {

    private static final Integer CLIENT_ID = 18421;
    private static final String SECRET_KEY = "iBIehjui12aK8x82oe5qzGeb0As=";
    private static final String OTP = "cccccccvlidcnlednilgctgcvcjtivrjidfbdgrefcvi";

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
        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }

    @Test(expected = FailedLoginException.class)
    public void checkBadConfigAuthn() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(123456, "123456");
        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }

    @Test(expected = AccountNotFoundException.class)
    public void checkAccountNotFound() throws Exception {
        final YubiKeyAuthenticationHandler handler =
                new YubiKeyAuthenticationHandler(CLIENT_ID, SECRET_KEY);
        handler.setRegistry(new YubiKeyAccountRegistry() {
            @Override
            public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
                return false;
            }
        });

        handler.authenticate(new UsernamePasswordCredential("casuser", OTP));
    }
}


