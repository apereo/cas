package org.apereo.cas.authentication.sync;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.LdapPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
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
class LdapPasswordSynchronizationAuthenticationPostProcessorTests {

    @Nested
    class DefaultTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        void verifySyncFindsNoUser() {
            assertThrows(AuthenticationException.class, () -> {
                val sync = ldapPasswordSynchronizers.first();
                val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown123456", "password");
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
                sync.destroy();
            });
        }

        @Test
        void verifyBadCredential() {
            assertThrows(AuthenticationException.class, () -> {
                val properties = casProperties.getAuthn().getPasswordSync().getLdap().getFirst();
                val sync = new LdapPasswordSynchronizationAuthenticationPostProcessor(
                    new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(properties)), properties);
                val credentials = mock(Credential.class);
                assertFalse(sync.supports(credentials));
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
                sync.destroy();
            });
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.password-sync.ldap[0].password-synchronization-failure-fatal=false",
        "cas.authn.password-sync.ldap[0].password-attribute=unicodePwd"
    })
    class UnicodeAttributeTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        void verifySyncFailsWithUnicodePswd() {
            assertDoesNotThrow(() -> {
                val sync = ldapPasswordSynchronizers.first();
                val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casTest", "password");
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
            });
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.password-sync.ldap[0].password-attribute=st")
    class UnknownAttributeTests extends BaseLdapPasswordSynchronizationTests {
        @Test
        void verifyOperation() throws Throwable {
            val sync = ldapPasswordSynchronizers.first();
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("admin", "password");
            assertTrue(sync.supports(credentials));
            assertDoesNotThrow(() -> {
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction());
                sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                    CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
            });
        }
    }

}
