package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
@TestPropertySource(properties = {
    "cas.authn.spnego.ldap.ldap-url=ldap://localhost:10389",
    "cas.authn.spnego.ldap.base-dn=ou=people,dc=example,dc=org",
    "cas.authn.spnego.ldap.search-filter=host={host}",
    "cas.authn.spnego.ldap.bind-dn=cn=Directory Manager",
    "cas.authn.spnego.ldap.bind-credential=password",

    "cas.authn.attribute-repository.stub.attributes.uid=uid",
    "cas.authn.attribute-repository.stub.attributes.host=host",
    "cas.authn.attribute-repository.stub.attributes.mail=mail",

    "cas.authn.spnego.alternative-remote-host-attribute=",
    "cas.authn.spnego.ips-to-check-pattern=.+",
    "cas.authn.spnego.dns-timeout=0",
    "cas.authn.spnego.host-name-client-action-strategy=ldapSpnegoClientAction",
    "cas.authn.spnego.spnego-attribute-name=mail"
})
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        val c = new LDAPConnection("localhost", 10389,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(c, "ou=people,dc=example,dc=org");
    }
}
