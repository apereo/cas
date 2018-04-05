package org.apereo.cas.pm;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.LdapPasswordManagementConfiguration;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link LdapPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(ConditionalSpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    LdapPasswordManagementConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreUtilConfiguration.class
})
@DirtiesContext
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
@TestPropertySource(locations = {"classpath:/ldap-pm.properties"})
public class LdapPasswordManagementServiceTests {
    private static final int LDAP_PORT = 10389;

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Test
    public void verifyTokenCreationAndParsing() {
        final String token = passwordChangeService.createToken("casuser");
        assertNotNull(token);
        final String result = passwordChangeService.parseToken(token);
        assertEquals("casuser", result);
    }

    @Test
    public void verifyPasswordChangedFails() {
        final Credential credential = new UsernamePasswordCredential("caspm", "123456");
        final PasswordChangeBean bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Mellon");
        bean.setPassword("Mellon");
        assertFalse(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyFindEmail() {
        final String email = passwordChangeService.findEmail("caspm");
        assertEquals("caspm@example.org", email);
    }

    @Test
    public void verifyFindSecurityQuestions() {
        final Map questions = passwordChangeService.getSecurityQuestions("caspm");
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("RegisteredAddressQuestion"));
        assertTrue(questions.containsKey("PostalCodeQuestion"));
    }

    @BeforeClass
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));

        final LDAPConnection localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm.ldif").getInputStream(),
            "ou=people,dc=example,dc=org");
    }
}
