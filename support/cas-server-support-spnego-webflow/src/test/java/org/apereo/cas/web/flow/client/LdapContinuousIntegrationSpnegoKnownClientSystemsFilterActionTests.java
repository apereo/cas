package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.test.junit.EnabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(locations = {"classpath:/spnego.properties", "classpath:/spnego-ldap-ci.properties"})
@Tag("ldap")
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        val c = new LDAPConnection("localhost", 10389,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(c, "ou=people,dc=example,dc=org");
    }
}
