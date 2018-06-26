package org.apereo.cas.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class PersonDirectoryPrincipalResolverTests {

    private static final String ATTR_1 = "attr1";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyNullPrincipal() {
        final var resolver = new PersonDirectoryPrincipalResolver();
        final var p = resolver.resolve(() -> null, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNull(p);
    }

    @Test
    public void verifyNullAttributes() {
        final var resolver = new PersonDirectoryPrincipalResolver(true, CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var p = resolver.resolve(c, null);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        final var resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(),
                CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var p = resolver.resolve(c, null);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        final var resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var p = resolver.resolve(c, null);
        assertNotNull(p);
        assertNotEquals(p.getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        final var resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());

        final var chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("cn", "originalCN");
        attributes.put(ATTR_1, "value1");
        final var p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes)),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertNotEquals("originalCN", p.getAttributes().get("cn"));
    }

    @Test
    public void verifyChainingResolver() {
        final var resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());

        final var chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        final var p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, Collections.singletonMap(ATTR_1, "value"))),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
    }

    @Test
    public void verifyChainingResolverOverwritePrincipal() {
        final var resolver = new PersonDirectoryPrincipalResolver(
                CoreAuthenticationTestUtils.getAttributeRepository());
        final var resolver2 = new PersonDirectoryPrincipalResolver(
                new StubPersonAttributeDao(Collections.singletonMap("principal", CollectionUtils.wrap("changedPrincipal"))), "principal");

        final var chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        final var p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, "value"))),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("changedPrincipal", p.getId());
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertFalse(p.getAttributes().containsKey("principal"));
    }
}
