package org.apereo.cas.impl.account;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.LdapPasswordlessAuthenticationConfiguration;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.ldap.ldap-url=ldap://localhost:10389",
    "cas.authn.passwordless.accounts.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.passwordless.accounts.ldap.searchFilter=cn={0}",
    "cas.authn.passwordless.accounts.ldap.bindDn=cn=Directory Manager",
    "cas.authn.passwordless.accounts.ldap.bindCredential=password",
    "cas.authn.passwordless.accounts.ldap.email-attribute=mail",
    "cas.authn.passwordless.accounts.ldap.phone-attribute=telephoneNumber"
})
@Slf4j
@Import(LdapPasswordlessAuthenticationConfiguration.class)
public class LdapPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        @Cleanup
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        val resource = new ClassPathResource("ldif/ldap-passwordless.ldif");
        LOGGER.debug("Populating LDAP entries from [{}]", resource);
        LdapIntegrationTestsOperations.populateEntries(localhost, resource.getInputStream(), "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifyAction() {
        val user = passwordlessUserAccountStore.findUser("passwordlessuser");
        assertTrue(user.isPresent());
        assertEquals("passwordlessuser@example.org", user.get().getEmail());
        assertEquals("123456789", user.get().getPhone());
    }
}
