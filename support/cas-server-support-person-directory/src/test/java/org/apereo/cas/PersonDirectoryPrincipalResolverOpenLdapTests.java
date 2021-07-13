package org.apereo.cas;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
    "cas.authn.attribute-repository.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:11389",
    "cas.authn.attribute-repository.ldap[0].search-filter=cn={username}",
    "cas.authn.attribute-repository.ldap[0].trust-manager=ANY",
    "cas.authn.attribute-repository.ldap[0].attributes.homePostalAddress=homePostalAddress;",
    "cas.authn.attribute-repository.ldap[0].attributes.cn=cn",
    "cas.authn.attribute-repository.ldap[0].bind-dn=cn=admin,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].bind-credential=P@ssw0rd",
    "cas.authn.attribute-repository.ldap[0].attributes.sn=surname"
})
@Tag("Ldap")
@EnabledIfPortOpen(port = 11389)
public class PersonDirectoryPrincipalResolverOpenLdapTests {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyResolverWithTags() throws Exception {
        val bindInit = new BindConnectionInitializer("cn=admin,dc=example,dc=org", new Credential("P@ssw0rd"));
        @Cleanup
        val connection = new LDAPConnection("localhost", 11389,
            bindInit.getBindDn(), bindInit.getBindCredential().getString());

        val uid = UUID.randomUUID().toString();
        val ldif = getLdif(uid);
        val rs = new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8));
        LdapIntegrationTestsOperations.populateEntries(connection, rs, "ou=people,dc=example,dc=org", bindInit);

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory(),
            this.attributeRepository,
            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
            casProperties.getPersonDirectory());
        val p = resolver.resolve(new UsernamePasswordCredential(uid, "password"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(uid)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("homePostalAddress;lang-jp"));
        assertTrue(p.getAttributes().containsKey("homePostalAddress;lang-fr"));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertTrue(p.getAttributes().containsKey("surname"));
    }

    protected String getLdif(final String user) {
        val baseDn = casProperties.getAuthn().getAttributeRepository().getLdap().get(0).getBaseDn();
        return String.format("dn: cn=%s,%s%n"
            + "objectClass: top%n"
            + "objectClass: person%n"
            + "objectClass: organizationalPerson%n"
            + "objectClass: inetOrgPerson%n"
            + "cn: %s%n"
            + "homePostalAddress;lang-jp: address japan%n"
            + "homePostalAddress;lang-fr: 34 rue de Seine%n"
            + "userPassword: 123456%n"
            + "sn: %s%n"
            + "uid: %s%n", user, baseDn, user, user, user);
    }
}
