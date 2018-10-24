package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdaptiveTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.ldap[0].baseDn=dc=example,dc=org",
    "cas.authn.attributeRepository.ldap[0].ldapUrl=ldap://localhost:10389",
    "cas.authn.attributeRepository.ldap[0].searchFilter=cn={user}",
    "cas.authn.attributeRepository.ldap[0].useSsl=false",
    "cas.authn.attributeRepository.ldap[0].attributes.cn=cn",
    "cas.authn.attributeRepository.ldap[0].attributes.description=description",
    "cas.authn.attributeRepository.ldap[0].bindDn=cn=Directory Manager",
    "cas.authn.attributeRepository.ldap[0].bindCredential=password"
    })
@DirtiesContext
@Category(LdapCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class PersonDirectoryPrincipalResolverLdaptiveTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyResolver() {
        val resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
        val p = resolver.resolve(new UsernamePasswordCredential("admin", "password"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("description"));
    }

    @Test
    public void verifyChainedResolver() {
        val resolver = new PersonDirectoryPrincipalResolver(this.attributeRepository);
        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val attributes = new HashMap<String, Object>(2);
        attributes.put("a1", "v1");
        attributes.put("a2", "v2");
        val p = chain.resolve(new UsernamePasswordCredential("admin", "password"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("admin", attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("cn"));
        assertTrue(p.getAttributes().containsKey("a1"));
        assertTrue(p.getAttributes().containsKey("a2"));
    }
}
