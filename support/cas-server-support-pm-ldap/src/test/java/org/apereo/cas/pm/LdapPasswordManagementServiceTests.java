package org.apereo.cas.pm;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
    "cas.authn.pm.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.pm.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.pm.ldap[0].bind-credential=password",
    "cas.authn.pm.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.pm.ldap[0].search-filter=(|(cn={user})(mail={user}))",
    "cas.authn.pm.ldap[0].type=GENERIC",
    "cas.authn.pm.ldap[0].security-questions-attributes.registeredAddress=roomNumber",
    "cas.authn.pm.ldap[0].security-questions-attributes.postalCode=teletexTerminalIdentifier"
})
@EnabledIfPortOpen(port = 10389)
public class LdapPasswordManagementServiceTests extends BaseLdapPasswordManagementServiceTests {
    private static final int LDAP_PORT = 10389;

    @BeforeAll
    public static void bootstrap() throws Exception {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm.ldif").getInputStream(),
            "ou=people,dc=example,dc=org",
            new BindConnectionInitializer("cn=Directory Manager", new Credential("password")));
    }

    @Test
    public void verifyTokenCreationAndParsing() {
        val token = passwordChangeService.createToken(PasswordManagementQuery.builder().username("casuser").build());
        assertNotNull(token);
        val result = passwordChangeService.parseToken(token);
        assertEquals("casuser", result);
    }

    @Test
    public void verifyPasswordChangedFails() {
        val credential = new UsernamePasswordCredential("caspm", "123456");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("Mellon");
        bean.setPassword("Mellon");
        bean.setUsername(credential.getUsername());
        assertFalse(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyFindEmail() {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("caspm@example.org", email);
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("unknown").build()));
    }

    @Test
    public void verifyUser() {
        val uid = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("caspm@example.org").build());
        assertEquals("CasPasswordManagement", uid);
        assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().email("unknown").build()));
    }

    @Test
    public void verifyFindPhone() {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("1234567890", ph);
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("unknown").build()));
    }

    @Test
    public void verifyFindSecurityQuestions() {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("RegisteredAddressQuestion"));
        assertEquals("666", questions.get("RegisteredAddressQuestion"));
        assertTrue(questions.containsKey("PostalCodeQuestion"));
        assertEquals("1776", questions.get("PostalCodeQuestion"));
    }

    @Test
    public void verifySecurityQuestions() {
        val query = PasswordManagementQuery.builder().username("caspm").build();
        query.securityQuestion("Q1", "A1");
        query.securityQuestion("Q2", "A2");
        passwordChangeService.updateSecurityQuestions(query);
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
    }
}
