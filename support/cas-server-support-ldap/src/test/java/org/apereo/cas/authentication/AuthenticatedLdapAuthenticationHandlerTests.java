package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import javax.security.auth.login.AccountNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AUTHENTICATED",
    "cas.authn.ldap[0].ldapUrl=ldap://localhost:10389",
    "cas.authn.ldap[0].useSsl=false",
    "cas.authn.ldap[0].baseDn=dc=example,dc=org",
    "cas.authn.ldap[0].searchFilter=cn={user}",
    "cas.authn.ldap[0].bindDn=cn=Directory Manager",
    "cas.authn.ldap[0].bindCredential=password",
    "cas.authn.ldap[0].principalAttributeList=description,cn"
    })
@EnabledIfContinuousIntegration
public class AuthenticatedLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
    @Test
    public void verifyAuthenticateNotFound() throws Throwable {
        try {
            assertThrows(AccountNotFoundException.class, () -> {
                this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("notfound", "badpassword"))));
            });
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void verifyAuthenticateFailureNotFound() throws Throwable {
        assertNotEquals(handler.size(), 0);
        assertThrows(AccountNotFoundException.class, () -> {
            try {
                this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("bad", "bad"))));
            } catch (final Exception e) {
                throw e.getCause();
            }
        });
    }
}
