package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LdapPasswordSynchronizationAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.authn.passwordSync.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.authn.passwordSync.ldap[0].base-dn=dc=example,dc=org",
        "cas.authn.passwordSync.ldap[0].search-filter=cn={user}",
        "cas.authn.passwordSync.ldap[0].bind-dn=cn=Directory Manager",
        "cas.authn.passwordSync.ldap[0].bind-credential=password"
    })
@Tag("Ldap")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 10389)
public class LdapPasswordSynchronizationAuthenticationPostProcessorTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void setup() throws Exception {
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        localhost.connect("localhost", 10389);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(localhost, "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifySyncFailsWithUnicodePswd() {
        assertDoesNotThrow(() -> {
            val sync = new LdapPasswordSynchronizationAuthenticationPostProcessor(casProperties.getAuthn().getPasswordSync().getLdap().get(0));
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casTest", "password");
            sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
        });
    }

    @Test
    public void verifySyncFindsNoUser() {
        assertDoesNotThrow(() -> {
            val sync = new LdapPasswordSynchronizationAuthenticationPostProcessor(casProperties.getAuthn().getPasswordSync().getLdap().get(0));
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown123456", "password");
            sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
            sync.destroy();
        });
    }

    @Test
    public void verifyBadCredential() {
        assertDoesNotThrow(() -> {
            val sync = new LdapPasswordSynchronizationAuthenticationPostProcessor(casProperties.getAuthn().getPasswordSync().getLdap().get(0));
            val credentials = mock(Credential.class);
            assertFalse(sync.supports(credentials));
            sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
            sync.destroy();
        });
    }


    @Test
    public void verifyOperation() {
        val sync = getProcessorWithMockPasswordAttribute();
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("admin", "password");
        assertTrue(sync.supports(credentials));
        assertDoesNotThrow(() -> {
            sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                new DefaultAuthenticationTransactionFactory().newTransaction());

            sync.process(CoreAuthenticationTestUtils.getAuthenticationBuilder(),
                new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
        });
    }

    private LdapPasswordSynchronizationAuthenticationPostProcessor getProcessorWithMockPasswordAttribute() {
        return new LdapPasswordSynchronizationAuthenticationPostProcessor(casProperties.getAuthn().getPasswordSync().getLdap().get(0)) {
            @Override
            protected LdapAttribute getLdapPasswordAttribute(final UsernamePasswordCredential credential) {
                return new LdapAttribute("st", credential.getPassword());
            }
        };
    }
}
