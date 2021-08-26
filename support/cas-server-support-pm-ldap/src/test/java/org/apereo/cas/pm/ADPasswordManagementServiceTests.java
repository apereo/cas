package org.apereo.cas.pm;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ADPasswordManagementServiceTests}.
 * Change password handling is different for Active Directory and this test exercises that AD specific code branch.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Tag("Ldap")
@TestPropertySource(properties = {
    "cas.authn.pm.reset.sms.attributeName=telephoneNumber",
    "cas.authn.pm.ldap[0].ldap-url=ldaps://localhost:10636",
    "cas.authn.pm.ldap[0].bind-dn=CN=admin,CN=Users,DC=cas,DC=example,DC=org",
    "cas.authn.pm.ldap[0].bind-credential=P@ssw0rd",
    "cas.authn.pm.ldap[0].base-dn=CN=Users,DC=cas,DC=example,DC=org",
    "cas.authn.pm.ldap[0].search-filter=cn={user}",
    "cas.authn.pm.ldap[0].type=AD",
    "cas.authn.pm.ldap[0].security-questions-attributes.department=company",
    "cas.authn.pm.ldap[0].security-questions-attributes.description=physicalDeliveryOfficeName",
    "cas.authn.pm.ldap[0].trust-store=file:/tmp/adcacerts.jks",
    "cas.authn.pm.ldap[0].trust-store-type=JKS",
    "cas.authn.pm.ldap[0].trust-store-password=changeit",
    "cas.authn.pm.ldap[0].min-pool-size=0",
    "cas.authn.pm.ldap[0].hostname-verifier=DEFAULT"
})
@EnabledIfPortOpen(port = 10636)
public class ADPasswordManagementServiceTests extends BaseLdapPasswordManagementServiceTests {

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
    }

    @Test
    public void verifyPasswordReset() {
        val credential = new UsernamePasswordCredential("changepassword", StringUtils.EMPTY);
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("P@ssw0rdMellon");
        bean.setPassword("P@ssw0rdMellon");
        bean.setUsername(credential.getUsername());
        assertTrue(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyPasswordChange() {
        val credential = new UsernamePasswordCredential("changepasswordnoreset", "P@ssw0rd");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("P@ssw0rd2");
        bean.setPassword("P@ssw0rd2");
        bean.setUsername(credential.getUsername());
        assertTrue(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyFindEmail() {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals("changepassword@example.org", email);
    }

    @Test
    public void verifyFindPhone() {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals("1234567890", ph);
    }

    @Test
    public void verifyFindSecurityQuestions() {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("DepartmentQuestion"));
        assertEquals("CompanyAnswer", questions.get("DepartmentQuestion"));
        assertTrue(questions.containsKey("DescriptionQuestion"));
        assertEquals("PhysicalDeliveryOfficeAnswer", questions.get("DescriptionQuestion"));
    }
}
