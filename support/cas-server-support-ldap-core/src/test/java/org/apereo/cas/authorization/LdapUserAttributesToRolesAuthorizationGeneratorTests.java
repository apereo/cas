package org.apereo.cas.authorization;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ReturnAttributes;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LdapUserAttributesToRolesAuthorizationGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class LdapUserAttributesToRolesAuthorizationGeneratorTests {

    @BeforeAll
    public static void setup() throws Exception {
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        localhost.connect("localhost", 10389);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(localhost, "ou=people,dc=example,dc=org");
    }

    @Test
    public void verifyOperation() throws Exception {
        val ldap = new Ldap();
        ldap.setBaseDn("ou=people,dc=example,dc=org");
        ldap.setLdapUrl("ldap://localhost:10389");
        ldap.setBindDn("cn=Directory Manager");
        ldap.setBindCredential("password");

        List.of(ReturnAttributes.NONE, ReturnAttributes.ALL).forEach(ret -> {
            var searchOp = LdapUtils.newLdaptiveSearchOperation("ou=people,dc=example,dc=org",
                "cn={user}", List.of("casTest"), List.of(ret.value()));
            searchOp.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
            val generator = new LdapUserAttributesToRolesAuthorizationGenerator(searchOp, false, "unknown", "ROLE");
            var profile = new CommonProfile();
            profile.setId("casTest");

            val result = generator.generate(mock(WebContext.class), JEESessionStore.INSTANCE, profile);
            assertFalse(result.isEmpty());
            assertTrue(profile.getAttributes().isEmpty());
            assertTrue(profile.getRoles().isEmpty());
            assertTrue(profile.getPermissions().isEmpty());
        });
    }

    private static class Ldap extends AbstractLdapAuthenticationProperties {
        private static final long serialVersionUID = 7979417317490698363L;
    }
}
