package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.ldaptive.provider.jndi.JndiProvider;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test demonstrates using the AD type with the {@link JndiProvider}.
 * Using the Ldaptive {@link UnboundIDProvider} would fail because the name doesn't validate as a DN.
 * Login name is {@code sAMAccountName} (aka "short" windows login name), bind as DOMAIN\USERNAME.
 * This allows for a dnFormat that the UnboundID provider would not allow since it is not a valid DN.
 * Issues:
 *  - This configuration doesn't retrieve any attributes as part of the authentication.
 *  - If the pool passivator is {@code CLOSE}, and the both success and failure tests run, the failure test will
 *      fail with wrong exception type. If multiple success tests are added, every other test will fail.
 * @author Hal Deadman
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AD",
    "cas.authn.ldap[0].ldapUrl=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAP_URL,
    "cas.authn.ldap[0].useSsl=false",
    "cas.authn.ldap[0].useStartTls=true",
    "cas.authn.ldap[0].subtreeSearch=true",
    "cas.authn.ldap[0].baseDn=cn=Users,dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].dnFormat=CAS\\\\%s",
    "cas.authn.ldap[0].principalAttributeList=sAMAccountName,cn",
    "cas.authn.ldap[0].enhanceWithEntryResolver=true",
    "cas.authn.ldap[0].searchFilter=(sAMAccountName={user})",
    "cas.authn.ldap[0].poolPassivator=BIND",
    "cas.authn.ldap[0].minPoolSize=0",
    "cas.authn.ldap[0].providerClass=org.ldaptive.provider.jndi.JndiProvider",
    "cas.authn.ldap[0].trustStore=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trustStoreType=JKS",
    "cas.authn.ldap[0].trustStorePassword=changeit",
    "cas.authn.ldap[0].hostnameVerifier=DEFAULT"
})
@EnabledIfContinuousIntegration
public class ActiveDirectoryJndiSamAccountNameLdapAuthenticationHandlerTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {

    /**
     * This dnFormat can authenticate but it isn't bringing back any attributes.
     */
    @Override
    protected String[] getPrincipalAttributes() {
        return new String[0];
    }

}


