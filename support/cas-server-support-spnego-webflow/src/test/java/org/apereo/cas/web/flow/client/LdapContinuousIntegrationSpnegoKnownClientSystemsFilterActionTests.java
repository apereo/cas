package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

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
@TestPropertySource(properties = {
    "cas.authn.spnego.ldap.ldapUrl=ldap://localhost:10389",
    "cas.authn.spnego.ldap.useSsl=false",
    "cas.authn.spnego.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.spnego.ldap.searchFilter=host={host}",
    "cas.authn.spnego.ldap.bindDn=cn=Directory Manager",
    "cas.authn.spnego.ldap.bindCredential=password"
})
@Tag("Ldap")
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
