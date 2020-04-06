package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.springframework.test.context.TestPropertySource;


/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].password-policy.enabled=true",
    "cas.authn.ldap[0].password-policy.custom-policy-class=org.apereo.cas.authentication.TestAuthenticationResponseHandler"
})
@EnabledIfPortOpen(port = 10389)
public class CustomPasswordPolicyLdapAuthenticationHandlerTests extends DirectLdapAuthenticationHandlerTests {
}
