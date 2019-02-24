package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.LdapTest;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(properties = {
    "cas.authn.spnego.ldap.ldapUrl=${ldap.url}",
    "cas.authn.spnego.ldap.useSsl=false",
    "cas.authn.spnego.ldap.baseDn=${ldap.baseDn}",
    "cas.authn.spnego.ldap.searchFilter=host={host}",
    "cas.authn.spnego.ldap.bindDn=${ldap.managerDn}",
    "cas.authn.spnego.ldap.bindCredential=${ldap.managerPassword}",
    "cas.authn.attributeRepository.stub.attributes.uid=uid",
    "cas.authn.attributeRepository.stub.attributes.host=host",
    "cas.authn.attributeRepository.stub.attributes.mail=mail",
    "cas.authn.spnego.alternativeRemoteHostAttribute=",
    "cas.authn.spnego.ipsToCheckPattern=",
    "cas.authn.spnego.dnsTimeout=0",
    "cas.authn.spnego.hostNameClientActionStrategy=ldapSpnegoClientAction",
    "cas.authn.spnego.spnegoAttributeName=mail"
    })
@DisabledIfContinuousIntegration
public class LdapSpnegoKnownClientSystemsFilterActionTests extends BaseLdapSpnegoKnownClientSystemsFilterActionTests implements LdapTest {

    @BeforeAll
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(PORT);
    }
}
