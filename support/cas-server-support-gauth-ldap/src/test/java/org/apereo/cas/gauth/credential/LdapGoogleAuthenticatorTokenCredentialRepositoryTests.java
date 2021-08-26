package org.apereo.cas.gauth.credential;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link LdapGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.gauth.ldap.ldap-url=ldap://localhost:10389",
        "cas.authn.mfa.gauth.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.mfa.gauth.ldap.search-filter=cn={0}",
        "cas.authn.mfa.gauth.ldap.account-attribute-name=description",
        "cas.authn.mfa.gauth.ldap.bind-dn=cn=Directory Manager",
        "cas.authn.mfa.gauth.ldap.bind-credential=password",

        "cas.authn.mfa.gauth.crypto.enabled=true"
    })
@EnableScheduling
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
public class LdapGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseLdapGoogleAuthenticatorTokenCredentialRepositoryTests {
    @Override
    @SneakyThrows
    protected String getUsernameUnderTest() {
        val uid = super.getUsernameUnderTest();

        @Cleanup
        val c = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");

        val bindInit = new BindConnectionInitializer("cn=Directory Manager", new Credential("password"));

        val rs = new ByteArrayInputStream(getLdif(uid).getBytes(StandardCharsets.UTF_8));
        LdapIntegrationTestsOperations.populateEntries(c, rs, "ou=people,dc=example,dc=org", bindInit);

        return uid;
    }

    protected String getLdif(final String user) {
        val baseDn = getCasProperties().getAuthn().getMfa().getGauth().getLdap().getBaseDn();
        return String.format("dn: cn=%s,%s%n"
            + "objectClass: organizationalRole%n"
            + "objectClass: person%n"
            + "objectClass: account%n"
            + "cn: %s%n"
            + "userPassword: 123456%n"
            + "sn: %s%n"
            + "uid: %s%n", user, baseDn, user, user, user);
    }
}
