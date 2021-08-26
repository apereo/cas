package org.apereo.cas;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPersonDirectoryAttributeRepositoryPlanLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
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
    private static final String CN = UUID.randomUUID().toString();
    
    @Autowired
    @Qualifier("personDirectoryAttributeRepositoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @BeforeAll
    public static void bootstrap() throws Exception {
        @Cleanup
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        var input = IOUtils.toString(new ClassPathResource("ldif/ldap-repository.ldif").getInputStream(), StandardCharsets.UTF_8);
        input = input.replace("${cn}", CN);
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifyOperation() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(CN, "Mellon");
        val principal = personDirectoryPrincipalResolver.resolve(creds);
        assertNotNull(principal);
        assertTrue(principal.getAttributes().containsKey("cn"));
        assertTrue(principal.getAttributes().containsKey("mail"));
    }
}
