package org.apereo.cas.authorization;

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
import java.io.Serial;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapUserGroupsToRolesAuthorizationGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnabledIfListeningOnPort(port = 10389)
@Tag("LdapAttributes")
class LdapUserGroupsToRolesAuthorizationGeneratorTests {

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

        val searchOp = LdapUtils.newLdaptiveSearchOperation("ou=people,dc=example,dc=org", "cn={user}", List.of("casTest"));
        searchOp.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
        val groupSearchOp = LdapUtils.newLdaptiveSearchOperation("ou=people,dc=example,dc=org", "businessCategory={user}", List.of("casTest"));
        groupSearchOp.setConnectionFactory(searchOp.getConnectionFactory());

        val generator = new LdapUserGroupsToRolesAuthorizationGenerator(searchOp, false,
            "unknown", "GRP", groupSearchOp);
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casTest");

        val result = generator.apply(principal);
        assertTrue(result.isEmpty());
    }

    private static final class Ldap extends AbstractLdapAuthenticationProperties {
        @Serial
        private static final long serialVersionUID = 7979417317490698363L;
    }
}
