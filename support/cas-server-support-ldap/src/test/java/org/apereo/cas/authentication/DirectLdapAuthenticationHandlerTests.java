package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=DIRECT",
    "cas.authn.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.ldap[0].dn-format=cn=%s,dc=example,dc=org",
    "cas.authn.ldap[0].principal-attribute-list=description,cn",
    "cas.authn.ldap[0].enhance-with-entry-resolver=false"
    })
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class DirectLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
}
