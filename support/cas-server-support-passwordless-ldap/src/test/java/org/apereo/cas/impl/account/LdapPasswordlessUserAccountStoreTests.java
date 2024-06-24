package org.apereo.cas.impl.account;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasLdapPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("LdapRepository")
@EnabledIfListeningOnPort(port = 10389)
class LdapPasswordlessUserAccountStoreTests {

    @TestPropertySource(properties = {
        "cas.authn.passwordless.accounts.ldap.ldap-url=ldap://localhost:10389",
        "cas.authn.passwordless.accounts.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.passwordless.accounts.ldap.search-filter=cn={0}",
        "cas.authn.passwordless.accounts.ldap.bind-dn=cn=Directory Manager",
        "cas.authn.passwordless.accounts.ldap.bind-credential=password",
        "cas.authn.passwordless.accounts.ldap.email-attribute=mail",
        "cas.authn.passwordless.accounts.ldap.phone-attribute=telephoneNumber",
        "cas.authn.passwordless.accounts.ldap.username-attribute=mail",
        "cas.authn.passwordless.accounts.ldap.request-password-attribute=description"
    })
    @ImportAutoConfiguration(CasLdapPasswordlessAuthenticationAutoConfiguration.class)
    abstract static class BaseLdapTests extends BasePasswordlessUserAccountStoreTests {
        @Autowired
        @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
        protected PasswordlessUserAccountStore passwordlessUserAccountStore;

        @BeforeEach
        void setup() throws Exception {
            @Cleanup
            val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
            val resource = new ClassPathResource("ldif/ldap-passwordless.ldif");
            LdapIntegrationTestsOperations.populateEntries(localhost, resource.getInputStream(), "ou=people,dc=example,dc=org");
        }
    }

    @Nested
    class DefaultTests extends BaseLdapTests {
        @RetryingTest(3)
        void verifyAction() throws Throwable {
            val user = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
                .builder()
                .username("passwordlessUser")
                .build());
            assertTrue(user.isPresent());
            assertEquals("passwordlessuser@example.org", user.get().getEmail());
            assertEquals("123456789", user.get().getPhone());
            assertEquals("passwordlessuser@example.org", user.get().getUsername());
            assertTrue(user.get().isRequestPassword());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.passwordless.accounts.ldap.required-attribute=description",
        "cas.authn.passwordless.accounts.ldap.required-attribute-value=.*this.+is.+missing.*"
    })
    class MissingRequiredAttributesTests extends BaseLdapTests {
        @RetryingTest(3)
        void verifyAction() throws Throwable {
            val user = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
                .builder()
                .username("passwordlessUser")
                .build());
            assertFalse(user.isPresent());
        }
    }

}
