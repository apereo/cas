package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test uses {@link UnboundIDProvider} against Active Directory but it specifies the type as {@code AUTHENTICATED}.
 * The {@code AD} type requires a dnFormat and Ldaptive will try create an UnboundID DN object but some patterns
 * that {@code AD} will let you bind as (e.g. userPrincipalName@domain.name or Domain\sAMAccountName won't pass
 * UnboundID DN validation.
 * This test validates a configuration where the user logs in with the {@code sAMAccountName}.
 * Referrals are turned off since search is subtree search from root.
 * @author Hal Deadman
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AUTHENTICATED",
    "cas.authn.ldap[0].bindDn=Administrator@cas.example.org",
    "cas.authn.ldap[0].bindCredential=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_ADMIN_PASSWORD,
    "cas.authn.ldap[0].ldapUrl=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAPS_URL,
    "cas.authn.ldap[0].useSsl=true",
    "cas.authn.ldap[0].useStartTls=false",
    "cas.authn.ldap[0].subtreeSearch=true",
    "cas.authn.ldap[0].baseDn=dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].followReferrals=false",
    "cas.authn.ldap[0].principalAttributeList=sAMAccountName,cn",
    "cas.authn.ldap[0].enhanceWithEntryResolver=true",
    "cas.authn.ldap[0].searchFilter=(sAMAccountName={user})",
    "cas.authn.ldap[0].minPoolSize=0",
    "cas.authn.ldap[0].providerClass=org.ldaptive.provider.unboundid.UnboundIDProvider",
    "cas.authn.ldap[0].trustStore=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trustStoreType=JKS",
    "cas.authn.ldap[0].trustStorePassword=changeit",
    "cas.authn.ldap[0].hostnameVerifier=DEFAULT"
    })
@EnabledIfContinuousIntegration
public class ActiveDirectoryUnboundIDBindDnSSLLdapAuthenticationHandlerTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {

}
