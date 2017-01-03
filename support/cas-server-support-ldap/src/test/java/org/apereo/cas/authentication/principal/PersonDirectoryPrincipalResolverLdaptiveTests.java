package org.apereo.cas.authentication.principal;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.EchoingPrincipalResolver;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdaptiveTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(locations = {"/ldap-context.xml", "/resolver-context.xml"})
public class PersonDirectoryPrincipalResolverLdaptiveTests extends AbstractLdapTests {

    private static final String ATTR_NAME_PASSWORD = "userPassword";
    
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
            final String psw = entry.getAttribute(ATTR_NAME_PASSWORD).getStringValue();
            final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
            resolver.setAttributeRepository(this.attributeDao);
            final Principal p = resolver.resolve(new UsernamePasswordCredential(username, psw), CoreAuthenticationTestUtils.getPrincipal());
            assertNotNull(p);
            assertTrue(p.getAttributes().containsKey("displayName"));
        }
    }

    @Test
    public void verifyChainedResolver() {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            final String psw = entry.getAttribute(ATTR_NAME_PASSWORD).getStringValue();
            final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
            resolver.setAttributeRepository(this.attributeDao);

            final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
            chain.setChain(Arrays.asList(resolver, new EchoingPrincipalResolver()));
            final Map<String, Object> attributes = new HashMap<>(2);
            attributes.put("a1", "v1");
            attributes.put("a2", "v2");
            final Principal p = chain.resolve(new UsernamePasswordCredential(username, psw), CoreAuthenticationTestUtils.getPrincipal(username, attributes));
            assertNotNull(p);
            assertTrue(p.getAttributes().containsKey("displayName"));
            assertTrue(p.getAttributes().containsKey("a1"));
            assertTrue(p.getAttributes().containsKey("a2"));
        }
    }
}
