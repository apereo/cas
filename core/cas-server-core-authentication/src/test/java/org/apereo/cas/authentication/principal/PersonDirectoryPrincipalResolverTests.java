package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class PersonDirectoryPrincipalResolverTests {

    private static final String ATTR_1 = "attr1";

    @Test
    public void verifyNullPrincipal() {
        val resolver = new PersonDirectoryPrincipalResolver();
        val p = resolver.resolve(() -> null, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNull(p);
    }

    @Test
    public void verifyNullAttributes() {
        val resolver = new PersonDirectoryPrincipalResolver(true, CoreAuthenticationTestUtils.CONST_USERNAME);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, null);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(),
            CoreAuthenticationTestUtils.CONST_USERNAME);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, null);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, null);
        assertNotNull(p);
        assertNotEquals(p.getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());

        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val attributes = new HashMap<String, Object>();
        attributes.put("cn", "originalCN");
        attributes.put(ATTR_1, "value1");
        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertNotEquals("originalCN", p.getAttributes().get("cn"));
    }

    @Test
    public void verifyChainingResolver() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());

        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, Collections.singletonMap(ATTR_1, "value"))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
    }

    @Test
    public void verifyChainingResolverOverwritePrincipal() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver2 = new PersonDirectoryPrincipalResolver(
            new StubPersonAttributeDao(Collections.singletonMap("principal", CollectionUtils.wrap("changedPrincipal"))), "principal");

        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, "value"))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("changedPrincipal", p.getId());
        assertEquals(6, p.getAttributes().size());
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertTrue(p.getAttributes().containsKey("principal"));
    }

    @Test
    public void verifyMultiplePrincipalAttributeNames() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver2 = new PersonDirectoryPrincipalResolver(
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))), " invalid, something");
        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, "value"))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("principal-id", p.getId());
    }

    @Test
    public void verifyMultiplePrincipalAttributeNamesNotFound() {
        val resolver = new PersonDirectoryPrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val resolver2 = new PersonDirectoryPrincipalResolver(
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))), " invalid, ");
        val chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, "value"))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("test", p.getId());
    }
}
