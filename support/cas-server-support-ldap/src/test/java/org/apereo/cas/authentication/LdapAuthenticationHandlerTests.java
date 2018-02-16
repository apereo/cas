package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.LdapAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.jooq.lambda.Unchecked;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        LdapAuthenticationConfiguration.class})
@TestPropertySource(locations = {"classpath:/ldap.properties"})
public class LdapAuthenticationHandlerTests extends AbstractLdapTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("ldapAuthenticationHandlers")
    private Collection<AuthenticationHandler> handler;

    @Test
    public void verifyAuthenticateFailure() throws Throwable {
        assertNotEquals(handler.size(), 0);
        this.thrown.expect(FailedLoginException.class);
        try {
            this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("castest1", "bad"))));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void verifyAuthenticateSuccess() {
        assertNotEquals(handler.size(), 0);

        this.handler.forEach(Unchecked.consumer(h -> {
            final UsernamePasswordCredential credential = new UsernamePasswordCredential("castest1", "castest1");
            final HandlerResult result = h.authenticate(credential);
            assertNotNull(result.getPrincipal());
            assertEquals(credential.getUsername(), result.getPrincipal().getId());
            final Map<String, Object> attributes = result.getPrincipal().getAttributes();
            assertTrue(attributes.containsKey("givenName"));
            assertTrue(attributes.containsKey("mail"));
        }));

    }

    @Test
    public void verifyAuthenticateFailureNotFound() throws Throwable {
        assertNotEquals(handler.size(), 0);
        this.thrown.expect(AccountNotFoundException.class);
        try {
            this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("bad", "bad"))));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void verifyAuthenticateNotFound() throws Throwable {
        try {
            this.thrown.expect(AccountNotFoundException.class);
            this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("notfound", "badpassword"))));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

}
