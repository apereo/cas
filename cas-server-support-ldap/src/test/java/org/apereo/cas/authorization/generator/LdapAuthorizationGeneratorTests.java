package org.apereo.cas.authorization.generator;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;

import org.apereo.cas.config.LdapAuthenticationConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link LdapAuthorizationGenerator} class.
 * <p>
 * The virginiaTechGroup schema MUST be installed on the target directories prior to running this test.
 *
 * @author Marvin Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {LdapAuthenticationConfiguration.class, RefreshAutoConfiguration.class}, 
        locations={"/ldap-context.xml", "/ldap-authorizationgenerator-test.xml"})
public class LdapAuthorizationGeneratorTests extends AbstractLdapTests {

    private static final String CAS_SERVICE_DETAILS_OBJ_CLASS = "casServiceUserDetails";

    @Autowired
    @Qualifier("ldapAuthorizationGenerator")
    private AuthorizationGenerator ldapAuthorizationGenerator;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyLoadUserByUsername() throws Exception {
        getEntries().stream().filter(entry -> entry.getAttribute("objectclass").getStringValues()
                .contains(CAS_SERVICE_DETAILS_OBJ_CLASS)).forEach(entry -> {
            final String username = getUsername(entry);
            final CommonProfile profile = new CommonProfile();
            profile.setId(username);
            ldapAuthorizationGenerator.generate(profile);
            assertTrue(hasAuthority(profile, "ROLE_ADMINISTRATORS"));
            assertTrue(hasAuthority(profile, "ROLE_USERS"));
        });
    }

    private static boolean hasAuthority(final CommonProfile profile, final String name) {
        return profile.getRoles().contains(name);
    }
}
