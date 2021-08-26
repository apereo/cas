package org.apereo.cas.webauthn;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.config.LdapWebAuthnConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OpenLdapWebAuthnCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(
    properties = {
        "cas.authn.mfa.web-authn.ldap.ldap-url=ldap://localhost:11389",
        "cas.authn.mfa.web-authn.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.mfa.web-authn.ldap.search-filter=cn={0}",
        "cas.authn.mfa.web-authn.ldap.account-attribute-name=description",
        "cas.authn.mfa.web-authn.ldap.bind-dn=cn=admin,dc=example,dc=org",
        "cas.authn.mfa.web-authn.ldap.bind-credential=P@ssw0rd",
        "cas.authn.mfa.web-authn.ldap.trust-manager=ANY"
    })
@Tag("Ldap")
@EnabledIfPortOpen(port = 11636)
@Getter
@Import(LdapWebAuthnConfiguration.class)
public class OpenLdapWebAuthnCredentialRepositoryTests extends BaseWebAuthnCredentialRepositoryTests {

    @SneakyThrows
    @Override
    protected String getUsername() {
        val uid = super.getUsername();

        val bindInit = new BindConnectionInitializer("cn=admin,dc=example,dc=org", new Credential("P@ssw0rd"));

        @Cleanup
        val connection = new LDAPConnection("localhost", 11389,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        val rs = new ByteArrayInputStream(getLdif(uid).getBytes(StandardCharsets.UTF_8));
        LdapIntegrationTestsOperations.populateEntries(connection, rs, "ou=people,dc=example,dc=org", bindInit);
        return uid;
    }

    protected String getLdif(final String user) {
        val baseDn = casProperties.getAuthn().getMfa().getWebAuthn().getLdap().getBaseDn();
        return String.format("dn: cn=%s,%s%n"
            + "objectClass: top%n"
            + "objectClass: person%n"
            + "objectClass: organizationalPerson%n"
            + "objectClass: inetOrgPerson%n"
            + "cn: %s%n"
            + "userPassword: 123456%n"
            + "sn: %s%n"
            + "uid: %s%n", user, baseDn, user, user, user);
    }
}
