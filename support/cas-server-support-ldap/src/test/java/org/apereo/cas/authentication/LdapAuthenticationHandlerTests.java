package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.LdapAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.jooq.lambda.Unchecked;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Collection;

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
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        LdapAuthenticationConfiguration.class})
@TestPropertySource(locations = {"classpath:/ldap.properties"})
public class LdapAuthenticationHandlerTests extends AbstractLdapTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationHandlerTests.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("ldapAuthenticationHandlers")
    private Collection<AuthenticationHandler> handler;

    @BeforeClass
    public static void bootstrap() throws Exception {
        LOGGER.debug("Running [{}]", LdapAuthenticationHandlerTests.class.getSimpleName());
        initDirectoryServer();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        DIRECTORY.close();
    }

    @Test
    public void verifyAuthenticateSuccess() throws Exception {
        assertNotEquals(handler.size(), 0);
        getEntries().forEach(entry -> {
            final String username = entry.getAttribute("sAMAccountName").getStringValue();
            final String psw = entry.getAttribute("userPassword").getStringValue();

            this.handler.forEach(Unchecked.consumer(h -> {
                final HandlerResult result = h.authenticate(new UsernamePasswordCredential(username, psw));
                assertNotNull(result.getPrincipal());
                assertEquals(username, result.getPrincipal().getId());
                assertEquals(
                        entry.getAttribute("displayName").getStringValue(),
                        result.getPrincipal().getAttributes().get("displayName"));
                assertEquals(
                        entry.getAttribute("mail").getStringValue(),
                        result.getPrincipal().getAttributes().get("mail"));
            }));
        });
    }

    @Test
    public void verifyAuthenticateFailure() throws Throwable {
        assertNotEquals(handler.size(), 0);
        this.thrown.expect(FailedLoginException.class);
        try {
            this.getEntries().stream()
                    .map(entry -> entry.getAttribute("sAMAccountName").getStringValue())
                    .forEach(username -> this.handler.forEach(Unchecked.consumer(h -> {
                        h.authenticate(new UsernamePasswordCredential(username, "badpassword"));
                    })));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void verifyAuthenticateNotFound() throws Throwable {
        try {
            this.thrown.expect(AccountNotFoundException.class);
            this.handler.forEach(Unchecked.consumer(h -> {
                h.authenticate(new UsernamePasswordCredential("notfound", "badpassword"));
            }));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }
}
