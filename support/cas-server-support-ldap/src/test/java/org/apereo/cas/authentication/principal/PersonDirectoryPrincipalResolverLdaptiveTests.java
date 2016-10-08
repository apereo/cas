package org.apereo.cas.authentication.principal;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdaptiveTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(locations={"/ldap-context.xml", "/resolver-context.xml"})
public class PersonDirectoryPrincipalResolverLdaptiveTests extends AbstractLdapTests {

    @Autowired
    private LdaptivePersonAttributeDao attributeDao;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyResolver() {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            final String psw = entry.getAttribute("userPassword").getStringValue();
            final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
            resolver.setAttributeRepository(this.attributeDao);
            final Principal p = resolver.resolve(new UsernamePasswordCredential(username, psw));
            assertNotNull(p);
            assertTrue(p.getAttributes().containsKey("displayName"));
        }

    }

}
