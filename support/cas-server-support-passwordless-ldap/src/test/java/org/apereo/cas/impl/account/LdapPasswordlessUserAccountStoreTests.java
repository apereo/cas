package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasLdapPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.support.TriStateBoolean;
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
        "cas.authn.passwordless.accounts.ldap.request-password-attribute=description",
        "cas.authn.passwordless.accounts.ldap.multifactor-authentication-eligible-attribute=host",
        "cas.authn.passwordless.accounts.ldap.delegated-authentication-eligible-attribute=postalCode",
        "cas.authn.passwordless.accounts.ldap.allowed-delegated-clients-attribute=registeredAddress",
        "cas.authn.passwordless.accounts.ldap.allow-selection-menu-attribute=teletexTerminalIdentifier"
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
        void verifyUser1() throws Throwable {
            val optUser = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
                .builder()
                .username("passwordlessUser")
                .build());
            assertTrue(optUser.isPresent());
            val user = optUser.get();
            assertEquals("passwordlessuser@example.org", user.getEmail());
            assertEquals("123456789", user.getPhone());
            assertEquals("passwordlessuser@example.org", user.getUsername());
            assertTrue(user.isRequestPassword());
            assertEquals(TriStateBoolean.UNDEFINED, user.getMultifactorAuthenticationEligible());
            assertEquals(TriStateBoolean.UNDEFINED, user.getDelegatedAuthenticationEligible());
            assertEquals(List.of(), user.getAllowedDelegatedClients());
            assertFalse(user.isAllowSelectionMenu());

        }

        @RetryingTest(3)
        void verifyUser2() throws Throwable {
            val optUser = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
                    .builder()
                    .username("passwordlessUser2")
                    .build());
            assertTrue(optUser.isPresent());
            val user = optUser.get();
            assertEquals("passwordlessuser2@example.org", user.getEmail());
            assertEquals("987654321", user.getPhone());
            assertEquals("passwordlessuser2@example.org", user.getUsername());
            assertFalse(user.isRequestPassword());
            assertEquals(TriStateBoolean.TRUE, user.getMultifactorAuthenticationEligible());
            assertEquals(TriStateBoolean.TRUE, user.getDelegatedAuthenticationEligible());
            assertEquals(List.of("CAS1"), user.getAllowedDelegatedClients());
            assertTrue(user.isAllowSelectionMenu());
        }

        @RetryingTest(3)
        void verifyUser3() throws Throwable {
            val optUser = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
                    .builder()
                    .username("passwordlessUser3")
                    .build());
            assertTrue(optUser.isPresent());
            val user = optUser.get();
            assertEquals("passwordlessuser3@example.org", user.getEmail());
            assertEquals("0102030405", user.getPhone());
            assertEquals("passwordlessuser3@example.org", user.getUsername());
            assertFalse(user.isRequestPassword());
            assertEquals(TriStateBoolean.UNDEFINED, user.getMultifactorAuthenticationEligible());
            assertEquals(TriStateBoolean.UNDEFINED, user.getDelegatedAuthenticationEligible());
            assertEquals(List.of("CAS1", "CAS2"), user.getAllowedDelegatedClients());
            assertFalse(user.isAllowSelectionMenu());
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
