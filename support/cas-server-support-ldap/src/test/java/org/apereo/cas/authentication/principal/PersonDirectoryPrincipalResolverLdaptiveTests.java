package org.apereo.cas.authentication.principal;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
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
@SpringBootTest(classes = {CasPersonDirectoryConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/ldappersondir.properties"})
@DirtiesContext
public class PersonDirectoryPrincipalResolverLdaptiveTests extends AbstractLdapTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonDirectoryPrincipalResolverLdaptiveTests.class);

    private static final String ATTR_NAME_PASSWORD = "userPassword";

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @BeforeClass
    public static void bootstrap() throws Exception {
        LOGGER.debug("Running [{}]", PersonDirectoryPrincipalResolverLdaptiveTests.class.getSimpleName());
        initDirectoryServer(1385);
    }

    @Test
    public void verifyResolver() {
        if (getEntries() != null) {
            getEntries().forEach(entry -> {
                final String username = entry.getAttribute("sAMAccountName").getStringValue();
                final String psw = entry.getAttribute(ATTR_NAME_PASSWORD).getStringValue();
                final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
                final Principal p = resolver.resolve(new UsernamePasswordCredential(username, psw),
                        CoreAuthenticationTestUtils.getPrincipal(),
                        new SimpleTestUsernamePasswordAuthenticationHandler());
                assertNotNull(p);
                assertTrue(p.getAttributes().containsKey("displayName"));
            });
        }
    }

    @Test
    public void verifyChainedResolver() {
        if (getEntries() != null) {
            getEntries().forEach(entry -> {
                final String username = entry.getAttribute("sAMAccountName").getStringValue();
                final String psw = entry.getAttribute(ATTR_NAME_PASSWORD).getStringValue();
                final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
                final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
                chain.setChain(Arrays.asList(resolver, new EchoingPrincipalResolver()));
                final Map<String, Object> attributes = new HashMap<>(2);
                attributes.put("a1", "v1");
                attributes.put("a2", "v2");
                final Principal p = chain.resolve(new UsernamePasswordCredential(username, psw),
                        CoreAuthenticationTestUtils.getPrincipal(username, attributes),
                        new SimpleTestUsernamePasswordAuthenticationHandler());
                assertNotNull(p);
                assertTrue(p.getAttributes().containsKey("displayName"));
                assertTrue(p.getAttributes().containsKey("a1"));
                assertTrue(p.getAttributes().containsKey("a2"));
            });
        }
    }
}
