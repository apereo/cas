package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
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
    "ldap.managerDn=cn=Directory Manager,dc=example,dc=org",
    "ldap.managerPassword=Password",
    "cas.authn.spnego.ldap.ldapUrl=ldap://localhost:1381",
    "cas.authn.spnego.ldap.useSsl=false",
    "cas.authn.spnego.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.spnego.ldap.searchFilter=host={host}",
    "cas.authn.spnego.ldap.bindDn=${ldap.managerDn}",
    "cas.authn.spnego.ldap.bindCredential=${ldap.managerPassword}"
})
@DisabledIfContinuousIntegration
public class LdapSpnegoKnownClientSystemsFilterActionTests extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(1381);
    }
}
