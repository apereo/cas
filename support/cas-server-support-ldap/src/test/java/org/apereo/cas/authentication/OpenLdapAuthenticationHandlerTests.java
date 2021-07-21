package org.apereo.cas.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AUTHENTICATED",
    "cas.authn.ldap[0].ldap-url=ldap://localhost:11389",
    "cas.authn.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.ldap[0].search-filter=cn={user}",
    "cas.authn.ldap[0].bind-dn=cn=admin,dc=example,dc=org",
    "cas.authn.ldap[0].bind-credential=P@ssw0rd",
    "cas.authn.ldap[0].principal-attribute-list=sn,cn,homePostalAddress:homePostalAddress;"
})
@Tag("Ldap")
@EnabledIfPortOpen(port = 11389)
public class OpenLdapAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    protected String getLdif(final String user) {
        val baseDn = casProperties.getAuthn().getLdap().get(0).getBaseDn();
        return String.format("dn: cn=%s,%s%n"
            + "objectClass: top%n"
            + "objectClass: person%n"
            + "objectClass: organizationalPerson%n"
            + "objectClass: inetOrgPerson%n"
            + "cn: %s%n"
            + "homePostalAddress;lang-jp: address japan%n"
            + "homePostalAddress;lang-fr: 34 rue de Seine%n"
            + "userPassword: password%n"
            + "sn: %s%n"
            + "uid: %s%n", user, baseDn, user, user, user);
    }

    @Override
    String[] getPrincipalAttributes() {
        return new String[]{"sn", "cn", "homePostalAddress;lang-jp", "homePostalAddress;lang-fr"};
    }

    @Override
    @SneakyThrows
    String getUsername() {
        val bindInit = new BindConnectionInitializer("cn=admin,dc=example,dc=org", new Credential("P@ssw0rd"));
        @Cleanup
        val connection = new LDAPConnection("localhost", 11389,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        val uid = UUID.randomUUID().toString();
        val ldif = getLdif(uid);
        val rs = new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8));
        LdapIntegrationTestsOperations.populateEntries(connection, rs, "ou=people,dc=example,dc=org", bindInit);
        return uid;
    }

}
