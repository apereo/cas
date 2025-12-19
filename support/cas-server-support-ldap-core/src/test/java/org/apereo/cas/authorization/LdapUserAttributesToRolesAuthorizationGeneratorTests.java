package org.apereo.cas.authorization;

import module java.base;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ReturnAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapUserAttributesToRolesAuthorizationGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnabledIfListeningOnPort(port = 10389)
@Tag("LdapAttributes")
class LdapUserAttributesToRolesAuthorizationGeneratorTests {

    @BeforeAll
    public static void setup() throws Exception {
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        localhost.connect("localhost", 10389);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(localhost, "ou=people,dc=example,dc=org");
    }

    @Test
    void verifyOperation() throws Throwable {
        val ldap = new Ldap();
        ldap.setBaseDn("ou=people,dc=example,dc=org");
        ldap.setLdapUrl("ldap://localhost:10389");
        ldap.setBindDn("cn=Directory Manager");
        ldap.setBindCredential("password");

        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casTest");
        List.of(ReturnAttributes.NONE, ReturnAttributes.ALL).forEach(ret -> {
            val searchOp = LdapUtils.newLdaptiveSearchOperation("ou=people,dc=example,dc=org",
                "cn={user}", List.of("casTest"), List.of(ret.value()));
            searchOp.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
            val generator = new LdapUserAttributesToRolesAuthorizationGenerator(searchOp, false, "unknown", "ROLE");
            val result = generator.apply(principal);
            assertTrue(result.isEmpty());
        });
    }

    private static final class Ldap extends AbstractLdapAuthenticationProperties {
        @Serial
        private static final long serialVersionUID = 7979417317490698363L;
    }
}
