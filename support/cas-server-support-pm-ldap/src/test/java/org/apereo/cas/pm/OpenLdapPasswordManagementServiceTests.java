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
 * This is {@link OpenLdapPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@TestPropertySource(properties = {
    "cas.authn.pm.reset.sms.attribute-name=telephoneNumber",
    "cas.authn.pm.ldap[0].ldap-url=ldap://localhost:11389",
    "cas.authn.pm.ldap[0].bind-dn=cn=admin,dc=example,dc=org",
    "cas.authn.pm.ldap[0].bind-credential=P@ssw0rd",
    "cas.authn.pm.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.pm.ldap[0].search-filter=cn={0}",
    "cas.authn.pm.ldap[0].type=GENERIC",
    "cas.authn.pm.ldap[0].trust-manager=ANY",
    "cas.authn.pm.ldap[0].security-questions-attributes.registeredAddress=roomNumber"
})
@EnabledIfPortOpen(port = 11389)
public class OpenLdapPasswordManagementServiceTests extends BaseLdapPasswordManagementServiceTests {
    private static final int LDAP_PORT = 11389;

    @BeforeAll
    public static void bootstrap() throws Exception {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=admin,dc=example,dc=org", "P@ssw0rd");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/openldap-pm.ldif").getInputStream(),
            "ou=people,dc=example,dc=org",
            new BindConnectionInitializer("cn=admin,dc=example,dc=org", new Credential("P@ssw0rd")));
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
        assertTrue(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyFindEmail() {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("caspm@example.org", email);
    }

    @Test
    public void verifyFindPhone() {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals("1234567890", ph);
    }

    @Test
    public void verifyFindSecurityQuestions() {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("caspm").build());
        assertEquals(1, questions.size());
        assertTrue(questions.containsKey("RegisteredAddressQuestion"));
        assertEquals("666", questions.get("RegisteredAddressQuestion"));
    }
}
