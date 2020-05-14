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
    "cas.authn.ldap[0].additional-attributes=cn",
    "cas.authn.ldap[0].credential-criteria=.+",
    "cas.authn.ldap[0].principal-attribute-id=cn",
    "cas.authn.ldap[0].password-policy.enabled=true",
    "cas.authn.ldap[0].password-policy.type=FreeIPA",
    "cas.authn.ldap[0].password-policy.strategy=REJECT_RESULT_CODE",
    "cas.authn.ldap[0].password-policy.accountStateHandlingEnabled=false"
})
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class FreeIPAPasswordPolicyLdapAuthenticationHandlerTests extends DirectLdapAuthenticationHandlerTests {
}
