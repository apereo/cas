package org.apereo.cas.aup;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasAcceptableUsagePolicyLdapConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link LdapAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(LdapCategory.class)
@Import(CasAcceptableUsagePolicyLdapConfiguration.class)
@EnabledIfContinuousIntegration
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.ldap.ldapUrl=ldap://localhost:10389",
    "cas.acceptableUsagePolicy.ldap.useSsl=false",
    "cas.acceptableUsagePolicy.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.acceptableUsagePolicy.ldap.searchFilter=cn={0}",
    "cas.acceptableUsagePolicy.ldap.bindDn=cn=Directory Manager",
    "cas.acceptableUsagePolicy.ldap.bindCredential=password",
    "cas.acceptableUsagePolicy.aupAttributeName=carLicense"
})
@Getter
public class LdapAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    private static final int LDAP_PORT = 10389;

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        @Cleanup
        val localhost = new LDAPConnection("localhost", LDAP_PORT, "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-aup.ldif").getInputStream(), "ou=people,dc=example,dc=org");
    }
}
