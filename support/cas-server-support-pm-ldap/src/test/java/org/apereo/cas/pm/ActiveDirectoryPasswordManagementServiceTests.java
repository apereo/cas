package org.apereo.cas.pm;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
 * This is {@link ActiveDirectoryPasswordManagementServiceTests}.
 * Change password handling is different for Active Directory and this test exercises that AD specific code branch.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Tag("ActiveDirectory")
@TestPropertySource(properties = {
    "cas.authn.pm.reset.sms.attribute-name=telephoneNumber",
    "cas.authn.pm.core.enabled=true",
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
    "cas.authn.pm.ldap[0].hostname-verifier=ANY",
    "cas.authn.pm.ldap[0].trust-manager=ANY"
})
@EnabledIfListeningOnPort(port = 10636)
class ActiveDirectoryPasswordManagementServiceTests extends BaseLdapPasswordManagementServiceTests {

    @BeforeAll
        public static void bootstrap() {
        ClientInfoHolder.setClientInfo(ClientInfo.from(new MockHttpServletRequest()));
    }

    @Test
    void verifyPasswordReset() throws Throwable {
        val credential = new UsernamePasswordCredential("changepassword", StringUtils.EMPTY);
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("P@ssw0rdMellon".toCharArray());
        bean.setPassword("P@ssw0rdMellon".toCharArray());
        bean.setUsername(credential.getUsername());
        assertTrue(passwordChangeService.change(bean));
    }

    @Test
    void verifyPasswordChange() throws Throwable {
        val credential = new UsernamePasswordCredential("changepasswordnoreset", "P@ssw0rd");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("P@ssw0rd2".toCharArray());
        bean.setPassword("P@ssw0rd2".toCharArray());
        bean.setUsername(credential.getUsername());
        assertTrue(passwordChangeService.change(bean));
    }

    @Test
    void verifyFindEmail() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals("changepassword@example.org", email);
    }

    @Test
    void verifyFindPhone() throws Throwable {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals("1234567890", ph);
    }

    @Test
    void verifyFindSecurityQuestions() throws Throwable {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("changepassword").build());
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("DepartmentQuestion"));
        assertEquals("CompanyAnswer", questions.get("DepartmentQuestion"));
        assertTrue(questions.containsKey("DescriptionQuestion"));
        assertEquals("PhysicalDeliveryOfficeAnswer", questions.get("DescriptionQuestion"));
    }
}
