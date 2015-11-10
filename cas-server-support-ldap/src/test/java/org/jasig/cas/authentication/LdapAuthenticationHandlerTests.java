package org.jasig.cas.authentication;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/authn-context.xml"})
public class LdapAuthenticationHandlerTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("ldapAuthenticationHandler")
    private AuthenticationHandler handler;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyAuthenticateSuccess() throws Exception {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            final String psw = entry.getAttribute("userPassword").getStringValue();
            final HandlerResult result = this.handler.authenticate(
                    new UsernamePasswordCredential(username, psw));
            assertNotNull(result.getPrincipal());
            assertEquals(username, result.getPrincipal().getId());
            assertEquals(
                    entry.getAttribute("displayName").getStringValue(),
                    result.getPrincipal().getAttributes().get("displayName"));
            assertEquals(
                    entry.getAttribute("mail").getStringValue(),
                    result.getPrincipal().getAttributes().get("mail"));
        }
    }

    @Test(expected=FailedLoginException.class)
    public void verifyAuthenticateFailure() throws Exception {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            this.handler.authenticate(new UsernamePasswordCredential(username, "badpassword"));
            fail("Should have thrown FailedLoginException.");

        }
    }

    @Test(expected=AccountNotFoundException.class)
    public void verifyAuthenticateNotFound() throws Exception {
        this.handler.authenticate(new UsernamePasswordCredential("notfound", "somepwd"));
        fail("Should have thrown FailedLoginException.");
    }
}
