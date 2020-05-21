package org.apereo.cas;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlanLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.attribute-repository.ldap[0].base-dn=ou=people,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].search-filter=cn={0}",
    "cas.authn.attribute-repository.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.attribute-repository.ldap[0].bind-credential=password",
    "cas.authn.attribute-repository.ldap[0].attributes.cn=cn",
    "cas.authn.attribute-repository.ldap[0].attributes.mail=mail"
})
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
public class DefaultPersonDirectoryAttributeRepositoryPlanLdapTests {
    @Autowired
    @Qualifier("personDirectoryAttributeRepositoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        @Cleanup
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-repository.ldif").getInputStream(), "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifyOperation() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("ldapuser", "Mellon");
        val principal = personDirectoryPrincipalResolver.resolve(creds);
        assertNotNull(principal);
        assertTrue(principal.getAttributes().containsKey("cn"));
        assertTrue(principal.getAttributes().containsKey("mail"));
    }
}
