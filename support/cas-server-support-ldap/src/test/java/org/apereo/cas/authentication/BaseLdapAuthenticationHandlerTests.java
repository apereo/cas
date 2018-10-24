package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.category.LdapCategory;
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

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
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
    LdapAuthenticationConfiguration.class
})
@Category(LdapCategory.class)
public abstract class BaseLdapAuthenticationHandlerTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("ldapAuthenticationHandlers")
    protected Collection<AuthenticationHandler> handler;

    @Test
    public void verifyAuthenticateFailure() throws Throwable {
        assertNotEquals(handler.size(), 0);
        this.thrown.expect(FailedLoginException.class);
        try {
            this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("admin", "bad"))));
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void verifyAuthenticateSuccess() {
        assertNotEquals(handler.size(), 0);

        this.handler.forEach(Unchecked.consumer(h -> {
            val credential = new UsernamePasswordCredential("admin", "password");
            val result = h.authenticate(credential);
            assertNotNull(result.getPrincipal());
            assertEquals(credential.getUsername(), result.getPrincipal().getId());
            val attributes = result.getPrincipal().getAttributes();
            assertTrue(attributes.containsKey("cn"));
            assertTrue(attributes.containsKey("description"));
        }));

    }
}
