package org.apereo.cas.authentication.sync;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LdapPasswordSynchronizationAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@Tag("Ldap")
@EnabledIfListeningOnPort(port = 10389)
public class LdapPasswordSynchronizationAuthenticationPostProcessorTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        public void verifySyncFindsNoUser() {
            assertThrows(AuthenticationException.class, () -> {
                val sync = ldapPasswordSynchronizers.first();
                val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown123456", "password");
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
                sync.destroy();
            });
        }

        @Test
        public void verifyBadCredential() {
            assertThrows(AuthenticationException.class, () -> {
                val sync = new LdapPasswordSynchronizationAuthenticationPostProcessor(casProperties.getAuthn().getPasswordSync().getLdap().get(0));
                val credentials = mock(Credential.class);
                assertFalse(sync.supports(credentials));
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
                sync.destroy();
            });
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.password-sync.ldap[0].password-synchronization-failure-fatal=false",
        "cas.authn.password-sync.ldap[0].password-attribute=unicodePwd"
    })
    public class UnicodeAttributeTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        public void verifySyncFailsWithUnicodePswd() {
            assertDoesNotThrow(() -> {
                val sync = ldapPasswordSynchronizers.first();
                val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casTest", "password");
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
            });
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.password-sync.ldap[0].password-attribute=st")
    public class UnknownAttributeTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        public void verifyOperation() {
            val sync = ldapPasswordSynchronizers.first();
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("admin", "password");
            assertTrue(sync.supports(credentials));
            assertDoesNotThrow(() -> {
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    new DefaultAuthenticationTransactionFactory().newTransaction());
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
            });
        }
    }

}
