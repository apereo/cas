package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.LdapAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.security.auth.login.FailedLoginException;

import java.util.Arrays;
import java.util.Collection;

import static org.apereo.cas.util.junit.Assertions.assertThrowsWithRootCause;
import static org.junit.jupiter.api.Assertions.*;

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
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    LdapAuthenticationConfiguration.class
})
@Tag("Ldap")
public abstract class BaseLdapAuthenticationHandlerTests {
    @Autowired
    @Qualifier("ldapAuthenticationHandlers")
    protected Collection<AuthenticationHandler> handler;

    @Test
    public void verifyAuthenticateFailure() {
        assertNotEquals(handler.size(), 0);
        assertThrowsWithRootCause(UncheckedException.class, FailedLoginException.class,
            () -> this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential(getUsername(), getFailurePassword())))));
    }

    @Test
    public void verifyAuthenticateSuccess() {
        assertNotEquals(handler.size(), 0);

        this.handler.forEach(Unchecked.consumer(h -> {
            val credential = new UsernamePasswordCredential(getUsername(), getSuccessPassword());
            val result = h.authenticate(credential);
            assertNotNull(result.getPrincipal());
            assertEquals(credential.getUsername(), result.getPrincipal().getId());
            val attributes = result.getPrincipal().getAttributes();
            Arrays.stream(getPrincipalAttributes()).forEach(s -> assertTrue(attributes.containsKey(s)));
        }));
    }

    String[] getPrincipalAttributes() {
        return new String[]{"cn", "description"};
    }

    String getUsername() {
        return "admin";
    }

    String getSuccessPassword() {
        return "password";
    }

    String getFailurePassword() {
        return "bad";
    }
}
