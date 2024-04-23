package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

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
    "cas.authn.ldap[0].password-policy.enabled=true",
    "cas.authn.ldap[0].password-policy.type=EDirectory",
    "cas.authn.ldap[0].password-policy.strategy=GROOVY",
    "cas.authn.ldap[0].password-policy.groovy.location=classpath:GroovyPwdPolicy.groovy",
    "cas.authn.ldap[0].password-policy.account-state-handling-enabled=true",
    "cas.authn.ldap[0].password-policy.warning-attribute-name=attr-name",
    "cas.authn.ldap[0].password-policy.warning-attribute-value=attr-value"
})
@EnabledIfListeningOnPort(port = 10389)
@Tag("LdapAuthentication")
class EDirectoryPasswordPolicyLdapAuthenticationHandlerTests extends DirectLdapAuthenticationHandlerTests {
}
