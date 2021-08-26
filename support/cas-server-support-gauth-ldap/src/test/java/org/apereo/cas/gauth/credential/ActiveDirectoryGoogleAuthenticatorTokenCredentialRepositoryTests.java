package org.apereo.cas.gauth.credential;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.gauth.ldap.account-attribute-name=streetAddress",

        "cas.authn.mfa.gauth.ldap.ldap-url=ldaps://localhost:10636",
        "cas.authn.mfa.gauth.ldap.bind-dn=CN=admin,CN=Users,DC=cas,DC=example,DC=org",
        "cas.authn.mfa.gauth.ldap.bind-credential=P@ssw0rd",
        "cas.authn.mfa.gauth.ldap.base-dn=CN=Users,DC=cas,DC=example,DC=org",
        "cas.authn.mfa.gauth.ldap.search-filter=cn={user}",
        "cas.authn.mfa.gauth.ldap.trust-store=file:/tmp/adcacerts.jks",
        "cas.authn.mfa.gauth.ldap.trust-store-type=JKS",
        "cas.authn.mfa.gauth.ldap.trust-store-password=changeit",
        "cas.authn.mfa.gauth.ldap.min-pool-size=0",
        "cas.authn.mfa.gauth.ldap.hostname-verifier=DEFAULT",
        "cas.authn.mfa.gauth.crypto.enabled=true"
    })
@EnableScheduling
@Tag("Ldap")
@EnabledIfPortOpen(port = 10636)
public class ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests {
    @Override
    @SneakyThrows
    protected String getUsernameUnderTest() {
        val uid = "aham";

        val bindInit = new BindConnectionInitializer("CN=admin,CN=Users,DC=cas,DC=example,DC=org", new Credential("P@ssw0rd"));

        val sslUtil = new SSLUtil(null, new TrustAllTrustManager());
        val socketFactory = sslUtil.createSSLSocketFactory();

        @Cleanup
        val c = new LDAPConnection(socketFactory, "localhost", 10636,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        val mod = new Modification(ModificationType.REPLACE, "streetAddress", " ");
        c.modify(String.format("CN=%s,CN=Users,DC=cas,DC=example,DC=org", uid), mod);

        return uid;
    }
}
