package org.apereo.cas.aup;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.config.CasAcceptableUsagePolicyLdapConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.IOUtils;
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
@Import(CasAcceptableUsagePolicyLdapConfiguration.class)
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.acceptable-usage-policy.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.acceptable-usage-policy.ldap[0].search-filter=cn={0}",
    "cas.acceptable-usage-policy.ldap[0].bind-dn=cn=Directory Manager",
    "cas.acceptable-usage-policy.ldap[0].bind-credential=password",
    "cas.acceptable-usage-policy.core.aup-attribute-name=carLicense"
})
@Getter
public class LdapAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    private static final String USER = RandomUtils.randomAlphabetic(10);

    private static final int LDAP_PORT = 10389;

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @BeforeAll
    public static void bootstrap() throws Exception {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        @Cleanup
        val localhost = new LDAPConnection("localhost", LDAP_PORT, "cn=Directory Manager", "password");

        val ldif = IOUtils.toString(new ClassPathResource("ldif/ldap-aup.ldif").getInputStream(), StandardCharsets.UTF_8)
            .replace("$user", USER);
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8)),
            "ou=people,dc=example,dc=org");
    }

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }

    @Test
    public void verifyMissingUser() {
        val actualPrincipalId = UUID.randomUUID().toString();
        val c = getCredential(actualPrincipalId);
        val context = getRequestContext(actualPrincipalId, Map.of(), c);
        assertFalse(getAcceptableUsagePolicyRepository().verify(context).isAccepted());
    }

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction(USER,
            CollectionUtils.wrap("carLicense", List.of("false"), "email", List.of("casaupldap@example.org")));
    }
}
