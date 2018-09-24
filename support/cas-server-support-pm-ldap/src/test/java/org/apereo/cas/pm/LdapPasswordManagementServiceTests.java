package org.apereo.cas.pm;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.LdapPasswordManagementConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

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
@TestPropertySource(properties = {
    "cas.authn.pm.ldap.ldapUrl=ldap://localhost:10389",
    "cas.authn.pm.ldap.bindDn=cn=Directory Manager",
    "cas.authn.pm.ldap.bindCredential=password",
    "cas.authn.pm.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.pm.ldap.searchFilter=cn={user}",
    "cas.authn.pm.ldap.useSsl=false",
    "cas.authn.pm.ldap.type=GENERIC",
    "cas.authn.pm.ldap.securityQuestionsAttributes.registeredAddress=roomNumber",
    "cas.authn.pm.ldap.securityQuestionsAttributes.postalCode=teletexTerminalIdentifier"
 })
public class LdapPasswordManagementServiceTests extends AbstractPasswordManagementTests {

    private static final int LDAP_PORT = 10389;

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @BeforeClass
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));

        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm-ou.ldif").getInputStream(),
            "dc=example,dc=org");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-pm.ldif").getInputStream(),
            "ou=pm,dc=example,dc=org");
    }
}
