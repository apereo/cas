package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.jooq.lambda.Unchecked;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import javax.security.auth.login.AccountNotFoundException;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(locations = {"classpath:/ldapauthn.properties"})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class AuthenticatedLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
    @Test
    public void verifyAuthenticateNotFound() throws Throwable {
        try {
            this.thrown.expect(AccountNotFoundException.class);
            this.handler.forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("notfound", "badpassword"))));
        } catch (final Exception e) {
            throw e.getCause();
        }
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
}
