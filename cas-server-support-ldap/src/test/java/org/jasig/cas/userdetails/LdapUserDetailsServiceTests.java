package org.jasig.cas.userdetails;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link LdapUserDetailsService} class.
 * <p>
 * The virginiaTechGroup schema MUST be installed on the target directories prior to running this test.
 *
 * @author Marvin Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/ldap-userdetails-test.xml"})
public class LdapUserDetailsServiceTests extends AbstractLdapTests {

    private static final String CAS_SERVICE_DETAILS_OBJ_CLASS = "casServiceUserDetails";

    @Autowired
    @Qualifier("ldapUserDetailsService")
    private LdapUserDetailsService userDetailsService;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyLoadUserByUsername() throws Exception {
        for (final LdapEntry entry : getEntries()) {

            if (entry.getAttribute("objectclass").getStringValues().contains(CAS_SERVICE_DETAILS_OBJ_CLASS)) {
                final String username = getUsername(entry);
                final UserDetails user = userDetailsService.loadUserByUsername(username);
                assertEquals(username, user.getUsername());
                assertTrue(hasAuthority(user, "ROLE_ADMINISTRATORS"));
                assertTrue(hasAuthority(user, "ROLE_USERS"));
            }
        }
    }

    private boolean hasAuthority(final UserDetails user, final String name) {
        for (final GrantedAuthority authority : user.getAuthorities()) {
            if (authority.getAuthority().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
