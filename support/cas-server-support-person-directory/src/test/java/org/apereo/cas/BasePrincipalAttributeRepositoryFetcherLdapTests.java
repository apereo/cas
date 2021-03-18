package org.apereo.cas;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeAll;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * This is {@link BasePrincipalAttributeRepositoryFetcherLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
properties = {
    "cas.authn.attribute-repository.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.attribute-repository.ldap[0].attributes.cn=cn",
    "cas.authn.attribute-repository.ldap[0].attributes.title=title",
    "cas.authn.attribute-repository.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.attribute-repository.ldap[0].bind-credential=password"
})
public class BasePrincipalAttributeRepositoryFetcherLdapTests {
    protected static final String UID = UUID.randomUUID().toString();

    @Autowired
    @Qualifier("aggregatingAttributeRepository")
    protected IPersonAttributeDao aggregatingAttributeRepository;

    @BeforeAll
    public static void beforeAll() throws Exception {
        val bindInit = new BindConnectionInitializer("cn=Directory Manager",
            new Credential("password"));
        @Cleanup
        val connection = new LDAPConnection("localhost", 10389,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        val ldif = String.format("dn: cn=%s,%s%n"
            + "objectClass: top%n"
            + "objectClass: person%n"
            + "objectClass: organizationalPerson%n"
            + "objectClass: inetOrgPerson%n"
            + "cn: %s%n"
            + "givenName: %s%n"
            + "title: %s%n"
            + "userPassword: password%n"
            + "sn: %s%n"
            + "uid: %s%n", UID, "ou=people,dc=example,dc=org", UID, UID, UID, UID, UID);

        val rs = new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8));
        LdapIntegrationTestsOperations.populateEntries(connection,
            rs, "ou=people,dc=example,dc=org", bindInit);
    }
}
