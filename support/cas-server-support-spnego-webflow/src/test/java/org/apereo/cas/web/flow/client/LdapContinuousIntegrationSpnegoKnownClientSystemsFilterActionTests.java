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
    "cas.authn.spnego.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.spnego.ldap.searchFilter=host={host}",
    "cas.authn.spnego.ldap.bindDn=cn=Directory Manager",
    "cas.authn.spnego.ldap.bindCredential=password",

    "cas.authn.attributeRepository.stub.attributes.uid=uid",
    "cas.authn.attributeRepository.stub.attributes.host=host",
    "cas.authn.attributeRepository.stub.attributes.mail=mail",

    "cas.authn.spnego.alternativeRemoteHostAttribute=",
    "cas.authn.spnego.ipsToCheckPattern=.+",
    "cas.authn.spnego.dnsTimeout=0",
    "cas.authn.spnego.hostNameClientActionStrategy=ldapSpnegoClientAction",
    "cas.authn.spnego.spnegoAttributeName=mail"
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
