package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

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
    "cas.authn.ldap[0].ldapUrl=ldap://localhost:10389",
    "cas.authn.ldap[0].useSsl=false",
    "cas.authn.ldap[0].dnFormat=cn=%s,dc=example,dc=org",
    "cas.authn.ldap[0].principalAttributeList=description,cn",
    "cas.authn.ldap[0].enhanceWithEntryResolver=false"
    })
@EnabledIfContinuousIntegration
public class DirectLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
}
