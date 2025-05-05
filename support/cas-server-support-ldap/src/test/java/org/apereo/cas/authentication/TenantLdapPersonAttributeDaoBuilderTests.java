package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantLdapPersonAttributeDaoBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = BaseLdapAuthenticationHandlerTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@Tag("LdapAttributes")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 10389)
class TenantLdapPersonAttributeDaoBuilderTests {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver defaultPrincipalResolver;

    @BeforeAll
    public static void setup() throws Exception {
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        localhost.connect("localhost", 10389);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(localhost, "ou=people,dc=example,dc=org");
    }

    @Test
    void verifyResolver() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("mmoayyed");
        credential.getCredentialMetadata().setTenant("london");
        val principal = defaultPrincipalResolver.resolve(credential);
        assertNotNull(principal);
        assertEquals("mmoayyed", principal.getId());
        assertEquals("mmoayyed@example.org", principal.getSingleValuedAttribute("email"));
        assertEquals("mmoayyed", principal.getSingleValuedAttribute("cn"));
    }

}
