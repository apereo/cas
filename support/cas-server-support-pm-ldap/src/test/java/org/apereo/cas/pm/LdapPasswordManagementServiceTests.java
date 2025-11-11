package org.apereo.cas.pm;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@TestPropertySource(properties = {
    "cas.authn.pm.reset.sms.attribute-name=telephoneNumber",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.pm.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.pm.ldap[0].bind-credential=password",
    "cas.authn.pm.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.pm.ldap[0].search-filter=(|(cn={user})(mail={user}))",
    "cas.authn.pm.ldap[0].type=GENERIC",
    "cas.authn.pm.ldap[0].account-locked-attribute=businessCategory",
    "cas.authn.pm.ldap[0].security-questions-attributes.registeredAddress=roomNumber",
    "cas.authn.pm.ldap[0].security-questions-attributes.postalCode=teletexTerminalIdentifier"
})
@EnabledIfListeningOnPort(port = 10389)
class LdapPasswordManagementServiceTests extends BaseLdapPasswordManagementServiceTests {
    private static final int LDAP_PORT = 10389;

    @BeforeAll
    public static void bootstrap() throws Exception {
        ClientInfoHolder.setClientInfo(ClientInfo.from(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm.ldif").getInputStream(),
            "ou=people,dc=example,dc=org",
            new BindConnectionInitializer("cn=Directory Manager", new Credential("password")));
    }

    @Test
    void verifyTokenCreationAndParsing() {
        val token = passwordChangeService.createToken(PasswordManagementQuery.builder().username("casuser").build());
        assertNotNull(token);
        val result = passwordChangeService.parseToken(token);
        assertEquals("casuser", result);
    }

    @Test
    void verifyPasswordChangedFails() throws Throwable {
        val credential = new UsernamePasswordCredential("caspm", "123456");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("Mellon".toCharArray());
        bean.setPassword("Mellon".toCharArray());
        bean.setUsername(credential.getUsername());
        assertFalse(passwordChangeService.change(bean));
    }

    @Test
    void verifyFindEmail() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("caspm@example.org", email);
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("unknown").build()));
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("invalid").build()));
    }

    @Test
    void verifyUnlockAccount() throws Throwable {
        val credential = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("caspm");
        assertTrue(passwordChangeService.unlockAccount(credential));
    }

    @Test
    void verifyUser() throws Throwable {
        val uid = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("caspm@example.org").build());
        assertEquals("CasPasswordManagement", uid);
        assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().email("unknown").build()));
    }

    @Test
    void verifyFindPhone() throws Throwable {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("1234567890", ph);
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("unknown").build()));
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("invalid").build()));
    }

    @Test
    void verifyFindSecurityQuestions() throws Throwable {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("RegisteredAddressQuestion"));
        assertEquals("666", questions.get("RegisteredAddressQuestion"));
        assertTrue(questions.containsKey("PostalCodeQuestion"));
        assertEquals("1776", questions.get("PostalCodeQuestion"));
    }

    @Test
    void verifySecurityQuestions() throws Throwable {
        val query = PasswordManagementQuery.builder().username("caspm").build();
        query.securityQuestion("Q1", "A1");
        query.securityQuestion("Q2", "A2");
        passwordChangeService.updateSecurityQuestions(query);
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
    }
}
