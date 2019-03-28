package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test shows that {@link UnboundIDProvider} works with the type set to {@code AD} if the dnFormat is a standard DN.
 * This method doesn't required a bind DN but it requires users to all be in the same folder or organization unit.
 * This test assumes the user is logging in with the CN.
 * @author Hal Deadman
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AD",
    "cas.authn.ldap[0].ldapUrl=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAP_URL,
    "cas.authn.ldap[0].useSsl=false",
    "cas.authn.ldap[0].useStartTls=true",
    "cas.authn.ldap[0].subtreeSearch=true",
    "cas.authn.ldap[0].baseDn=dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].dnFormat=cn=%s,cn=Users,dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].principalAttributeList=sAMAccountName,cn",
    "cas.authn.ldap[0].enhanceWithEntryResolver=false",
    "cas.authn.ldap[0].minPoolSize=0",
    "cas.authn.ldap[0].providerClass=org.ldaptive.provider.unboundid.UnboundIDProvider",
    "cas.authn.ldap[0].trustStore=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trustStoreType=JKS",
    "cas.authn.ldap[0].trustStorePassword=changeit",
    "cas.authn.ldap[0].hostnameVerifier=DEFAULT"
})
@EnabledIfContinuousIntegration
public class ActiveDirectoryUnboundIDTypeADAuthenticationHandlerTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {
}


