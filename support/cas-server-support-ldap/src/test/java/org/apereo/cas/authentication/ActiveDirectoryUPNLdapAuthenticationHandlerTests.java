package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test uses the type AD where the user logs in with the userPrincipalName attribute.
 * The userPrincipalName attribute is the format UPN_PREFIX@UPN_SUFFIX where UPN_PREFIX is the "long" username
 * and UPN_SUFFIX is a domain in the Active Directory forest or a domain listed in upnSuffixes attribute.
 * UPN_PREFIX does not have to be unique but it is unique when combined with UPN_SUFFIX.
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
    "cas.authn.ldap[0].baseDn=cn=Users,dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].dnFormat=%s",
    "cas.authn.ldap[0].principalAttributeList=sAMAccountName,cn",
    "cas.authn.ldap[0].enhance-with-entry-resolver=true",
    "cas.authn.ldap[0].searchFilter=(userPrincipalName={user})",
    "cas.authn.ldap[0].min-pool-size=0",
    "cas.authn.ldap[0].trust-store=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trust-store-type=JKS",
    "cas.authn.ldap[0].trust-store-password=changeit",
    "cas.authn.ldap[0].hostname-verifier=ANY",
    "cas.authn.ldap[0].trust-manager=ANY"
})
@EnabledIfPortOpen(port = 10390)
@Tag("Ldap")
public class ActiveDirectoryUPNLdapAuthenticationHandlerTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {

    /**
     * This dnFormat can authenticate but it isn't bringing back any attributes.
     */
    @Override
    protected String[] getPrincipalAttributes() {
        return new String[0];
    }

    @Override
    protected String getUsername() {
        return "admin@cas.example.org";
    }

}


