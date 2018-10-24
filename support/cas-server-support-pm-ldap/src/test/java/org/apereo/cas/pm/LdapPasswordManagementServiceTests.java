package org.apereo.cas.pm;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.LdapPasswordManagementConfiguration;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link LdapPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(LdapCategory.class)
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

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final int LDAP_PORT = 10389;

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));

        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm.ldif").getInputStream(),
            "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifyTokenCreationAndParsing() {
        val token = passwordChangeService.createToken("casuser");
        assertNotNull(token);
        val result = passwordChangeService.parseToken(token);
        assertEquals("casuser", result);
    }

    @Test
    public void verifyPasswordChangedFails() {
        val credential = new UsernamePasswordCredential("caspm", "123456");
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Mellon");
        bean.setPassword("Mellon");
        assertFalse(passwordChangeService.change(credential, bean));
    }

    @Test
    public void verifyFindEmail() {
        val email = passwordChangeService.findEmail("caspm");
        assertEquals("caspm@example.org", email);
    }

    @Test
    public void verifyFindSecurityQuestions() {
        val questions = passwordChangeService.getSecurityQuestions("caspm");
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("RegisteredAddressQuestion"));
        assertEquals("666", questions.get("RegisteredAddressQuestion"));
        assertTrue(questions.containsKey("PostalCodeQuestion"));
        assertEquals("1776", questions.get("PostalCodeQuestion"));
    }
}
