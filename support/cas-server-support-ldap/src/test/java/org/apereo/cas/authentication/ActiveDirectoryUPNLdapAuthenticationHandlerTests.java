package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
    "cas.authn.ldap[0].useStartTls=true",
    "cas.authn.ldap[0].subtreeSearch=true",
    "cas.authn.ldap[0].baseDn=cn=Users,dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].dnFormat=%s",
    "cas.authn.ldap[0].principalAttributeList=sAMAccountName,cn",
    "cas.authn.ldap[0].enhanceWithEntryResolver=true",
    "cas.authn.ldap[0].searchFilter=(userPrincipalName={user})",
    "cas.authn.ldap[0].minPoolSize=0",
    "cas.authn.ldap[0].trustStore=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trustStoreType=JKS",
    "cas.authn.ldap[0].trustStorePassword=changeit",
    "cas.authn.ldap[0].hostnameVerifier=ANY"
})
@EnabledIfPortOpen(port = 10390)
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


