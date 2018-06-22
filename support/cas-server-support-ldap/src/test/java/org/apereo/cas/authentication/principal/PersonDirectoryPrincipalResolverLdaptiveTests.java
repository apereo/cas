package org.apereo.cas.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.util.Optional;

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
@Slf4j
public class PersonDirectoryPrincipalResolverLdaptiveTests {
    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyResolver() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
        final Principal p = resolver.resolve(new UsernamePasswordCredential("castest1", "castest1"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("givenName"));
    }

    @Test
    public void verifyChainedResolver() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        final Map<String, Object> attributes = new HashMap<>(2);
        attributes.put("a1", "v1");
        attributes.put("a2", "v2");
        final Principal p = chain.resolve(new UsernamePasswordCredential("castest1", "castest1"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("castest1", attributes)),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("givenName"));
        assertTrue(p.getAttributes().containsKey("a1"));
        assertTrue(p.getAttributes().containsKey("a2"));
    }
}
