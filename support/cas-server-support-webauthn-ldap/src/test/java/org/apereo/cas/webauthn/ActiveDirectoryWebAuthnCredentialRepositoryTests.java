package org.apereo.cas.webauthn;

import org.apereo.cas.config.LdapWebAuthnConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OpenLdapWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(
    properties = {
        "cas.authn.mfa.web-authn.ldap.account-attribute-name=streetAddress",
        "cas.authn.mfa.web-authn.ldap.ldap-url=ldaps://localhost:10636",
        "cas.authn.mfa.web-authn.ldap.bind-dn=CN=admin,CN=Users,DC=cas,DC=example,DC=org",
        "cas.authn.mfa.web-authn.ldap.bind-credential=P@ssw0rd",
        "cas.authn.mfa.web-authn.ldap.base-dn=CN=Users,DC=cas,DC=example,DC=org",
        "cas.authn.mfa.web-authn.ldap.search-filter=cn={user}",
        "cas.authn.mfa.web-authn.ldap.trust-store=file:/tmp/adcacerts.jks",
        "cas.authn.mfa.web-authn.ldap.trust-store-type=JKS",
        "cas.authn.mfa.web-authn.ldap.trust-store-password=changeit",
        "cas.authn.mfa.web-authn.ldap.min-pool-size=0",
        "cas.authn.mfa.web-authn.ldap.hostname-verifier=DEFAULT"
    })
@Tag("Ldap")
@EnabledIfPortOpen(port = 10636)
@Getter
@Import(LdapWebAuthnConfiguration.class)
public class ActiveDirectoryWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {
    @Override
    @SneakyThrows
    protected String getUsername() {
        val uid = super.getUsername();

        val bindInit = new BindConnectionInitializer("CN=admin,CN=Users,DC=cas,DC=example,DC=org", new Credential("P@ssw0rd"));

        val sslUtil = new SSLUtil(null, new TrustAllTrustManager());
        val socketFactory = sslUtil.createSSLSocketFactory();

        @Cleanup
        val c = new LDAPConnection(socketFactory, "localhost", 10636,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        c.add(getLdif(uid));
        val mod = new Modification(ModificationType.REPLACE, "streetAddress", " ");
        c.modify(String.format("CN=%s,CN=Users,DC=cas,DC=example,DC=org", uid), mod);

        return uid;
    }

    protected String[] getLdif(final String user) {
        val baseDn = casProperties.getAuthn().getMfa().getWebAuthn().getLdap().getBaseDn();
        return String.format("dn: cn=%s,%s;"
            + "objectClass: top;"
            + "objectClass: person;"
            + "objectClass: organizationalPerson;"
            + "objectClass: inetOrgPerson;"
            + "cn: %s;"
            + "userPassword: 123456;"
            + "sn: %s;"
            + "uid: %s", user, baseDn, user, user, user).split(";");
    }
}
