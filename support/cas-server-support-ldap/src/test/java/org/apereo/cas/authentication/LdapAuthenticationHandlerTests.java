package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

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
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"/ldap-context.xml", "/authn-context.xml"})
@TestPropertySource(locations = {"classpath:/ldap.properties"})
public class LdapAuthenticationHandlerTests extends AbstractLdapTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("ldapAuthenticationHandler")
    private AuthenticationHandler handler;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        DIRECTORY.close();
    }

    @Test
    public void verifyAuthenticateSuccess() throws Exception {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            final String psw = entry.getAttribute("userPassword").getStringValue();
            final HandlerResult result = this.handler.authenticate(new UsernamePasswordCredential(username, psw));
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

    @Test
    public void verifyAuthenticateFailure() throws Exception {
        for (final LdapEntry entry : this.getEntries()) {
            this.thrown.expect(FailedLoginException.class);

            final String username = getUsername(entry);
            this.handler.authenticate(new UsernamePasswordCredential(username, "badpassword"));
        }
    }

    @Test
    public void verifyAuthenticateNotFound() throws Exception {
        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("notfound not found.");

        this.handler.authenticate(new UsernamePasswordCredential("notfound", "somepwd"));
    }
}
