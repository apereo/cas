package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test demonstrates using the AD type.
 * Login name is {@code sAMAccountName} (aka "short" windows login name), bind as DOMAIN\USERNAME.
 * Issues:
 *  - This configuration doesn't retrieve any attributes as part of the authentication.
 * @author Hal Deadman
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AD",
    "cas.authn.ldap[0].ldap-url=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAP_URL,
    "cas.authn.ldap[0].use-start-tls=true",
    "cas.authn.ldap[0].subtree-search=true",
    "cas.authn.ldap[0].base-dn=cn=Users,dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].dn-format=CAS\\\\%s",
    "cas.authn.ldap[0].principal-attribute-list=sAMAccountName,cn",
    "cas.authn.ldap[0].enhance-with-entry-resolver=true",
    "cas.authn.ldap[0].search-filter=(sAMAccountName={user})",
    "cas.authn.ldap[0].pool-passivator=bind",
    "cas.authn.ldap[0].min-pool-size=0",
    "cas.authn.ldap[0].trust-store=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trust-store-type=JKS",
    "cas.authn.ldap[0].trust-store-password=changeit",
    "cas.authn.ldap[0].hostname-verifier=-default"
})
@EnabledIfPortOpen(port = 10390)
@Tag("Ldap")
public class ActiveDirectorySamAccountNameLdapAuthenticationHandlerTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {

    /**
     * This dnFormat can authenticate but it isn't bringing back any attributes.
     */
    @Override
    protected String[] getPrincipalAttributes() {
        return new String[0];
    }

}


