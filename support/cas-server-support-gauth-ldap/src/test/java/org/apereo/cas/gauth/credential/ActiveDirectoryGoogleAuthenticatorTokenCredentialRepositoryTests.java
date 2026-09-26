package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
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
        "cas.authn.mfa.gauth.ldap.account-attribute-name=description",

        "cas.authn.mfa.gauth.ldap.ldap-url=ldaps://localhost:10636",
        "cas.authn.mfa.gauth.ldap.bind-dn=" + ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests.BIND_DN,
        "cas.authn.mfa.gauth.ldap.bind-credential=" + ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests.BIND_CREDENTIAL,
        "cas.authn.mfa.gauth.ldap.base-dn=" + ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests.BASE_DN,
        "cas.authn.mfa.gauth.ldap.search-filter=cn={user}",
        "cas.authn.mfa.gauth.ldap.trust-store=file:${#systemProperties['java.io.tmpdir']}/adcacerts.jks",
        "cas.authn.mfa.gauth.ldap.trust-store-type=JKS",
        "cas.authn.mfa.gauth.ldap.trust-store-password=changeit",
        "cas.authn.mfa.gauth.ldap.min-pool-size=0",
        "cas.authn.mfa.gauth.ldap.hostname-verifier=ANY",
        "cas.authn.mfa.gauth.ldap.trust-manager=ANY",
        "cas.authn.mfa.gauth.crypto.enabled=true"
    })
@EnableScheduling
@Tag("ActiveDirectory")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 10636)
class ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests {
    static final String BASE_DN = "ou=gauth,dc=cas,dc=example,dc=org";

    static final String BIND_DN = "Administrator@cas.example.org";

    static final String BIND_CREDENTIAL = "M3110nM3110n#1";

    @BeforeAll
    static void createOrganizationalUnit() throws Exception {
        @Cleanup
        val connection = getConnection();
        try {
            connection.add(getOrganizationalUnitLdif(BASE_DN).split("\\R"));
        } catch (final LDAPException e) {
            if (!e.getResultCode().equals(ResultCode.ENTRY_ALREADY_EXISTS)) {
                throw e;
            }
        }
    }

    private static LDAPConnection getConnection() throws Exception {
        val socketFactory = new SSLUtil(null, new TrustAllTrustManager()).createSSLSocketFactory();
        return new LDAPConnection(socketFactory, "localhost", 10636, BIND_DN, BIND_CREDENTIAL);
    }

    @Override
    protected String getUsernameUnderTest() throws Exception {
        val uid = super.getUsernameUnderTest();
        @Cleanup
        val connection = getConnection();
        connection.add(getLdif(uid));
        return uid;
    }

    protected String[] getLdif(final String user) {
        return String.format("dn: cn=%s,%s;"
            + "objectClass: top;"
            + "objectClass: person;"
            + "objectClass: organizationalPerson;"
            + "objectClass: inetOrgPerson;"
            + "cn: %s;"
            + "userPassword: 123456;"
            + "sn: %s;"
            + "uid: %s", user, BASE_DN, user, user, user).split(";");
    }
}
