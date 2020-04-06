package org.apereo.cas.aup;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.config.CasAcceptableUsagePolicyLdapConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@Import(CasAcceptableUsagePolicyLdapConfiguration.class)
@EnabledIfPortOpen(port = 10389)
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.ldap[0].ldapUrl=ldap://localhost:10389",
    "cas.acceptableUsagePolicy.ldap[0].baseDn=ou=people,dc=example,dc=org",
    "cas.acceptableUsagePolicy.ldap[0].searchFilter=cn={0}",
    "cas.acceptableUsagePolicy.ldap[0].bindDn=cn=Directory Manager",
    "cas.acceptableUsagePolicy.ldap[0].bindCredential=password",
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

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("carLicense", List.of("false"),
                "email", List.of("CASuser@example.org")));
    }
}
