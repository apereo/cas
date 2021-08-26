package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
public class AcceptUsersAuthenticationHandlerTests {
    private static final String SCOTT = "scott";

    private static final String RUTGERS = "rutgers";

    @Test
    public void verifySupportsSpecialCharacters() throws Exception {
        val c = new UsernamePasswordCredential();
        c.setUsername("brian");
        c.setPassword("t�st");
        assertEquals("brian", getAuthenticationHandler().authenticate(c).getPrincipal().getId());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        val c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(RUTGERS);
        assertTrue(getAuthenticationHandler().supports(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            assertFalse(getAuthenticationHandler()
                .supports(new HttpBasedServiceCredential(new URL(
                    "http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService("https://some.app.edu"))));
        } catch (final MalformedURLException e) {
            throw new AssertionError("Could not resolve URL.", e);
        }
    }

    @Test
    public void verifyAuthenticatesUserInMap() {
        val c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(RUTGERS);

        try {
            assertEquals(SCOTT, getAuthenticationHandler().authenticate(c).getPrincipal().getId());
        } catch (final GeneralSecurityException e) {
            throw new AssertionError("Authentication exception caught but it should not have been thrown.", e);
        }
    }

    @Test
    public void verifyFailsUserNotInMap() {
        val c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword(RUTGERS);

        assertThrows(AccountNotFoundException.class, () -> getAuthenticationHandler().authenticate(c));
    }

    @Test
    public void verifyFailsNullUserName() {
        val c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        assertThrows(AccountNotFoundException.class, () -> getAuthenticationHandler().authenticate(c));
    }

    @Test
    public void verifyFailsNullUserNameAndPassword() {
        val c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);

        assertThrows(AccountNotFoundException.class, () -> getAuthenticationHandler().authenticate(c));
    }

    @Test
    public void verifyFailsNullPassword() {
        val c = new UsernamePasswordCredential();
        c.setUsername(SCOTT);
        c.setPassword(null);
        assertThrows(FailedLoginException.class, () -> getAuthenticationHandler().authenticate(c));
    }

    @Test
    public void verifyEmptyUsers() {
        val handler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, Map.of());
        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyNoPasswordStrategy() {
        val handler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, Map.of("another", "another"));
        handler.setPasswordPolicyHandlingStrategy(null);

        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("another")));
    }


    @Test
    public void verifyUserTransforms() {
        val handler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, Map.of("another", "another"));
        handler.setPrincipalNameTransformer(user -> null);

        assertThrows(AccountNotFoundException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("another")));
    }

    @Test
    public void verifyPasswordTransform() {
        val handler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, Map.of("another", "another"));
        handler.setPasswordEncoder(new PasswordEncoder() {
            @Override
            public String encode(final CharSequence charSequence) {
                return null;
            }

            @Override
            public boolean matches(final CharSequence charSequence, final String s) {
                return true;
            }
        });

        assertThrows(AccountNotFoundException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("another")));
    }

    @Test
    public void verifyCredentialPredicate() {
        val handler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, Map.of("another", "another"));
        handler.setCredentialSelectionPredicate(null);
        assertTrue(handler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("another")));
    }


    private static AuthenticationHandler getAuthenticationHandler() {
        val users = new HashMap<String, String>();
        users.put(SCOTT, RUTGERS);
        users.put("dima", "javarules");
        users.put("bill", "thisisAwesoME");
        users.put("brian", "t�st");

        return new AcceptUsersAuthenticationHandler(StringUtils.EMPTY,
            null, PrincipalFactoryUtils.newPrincipalFactory(), null, users);
    }
}
