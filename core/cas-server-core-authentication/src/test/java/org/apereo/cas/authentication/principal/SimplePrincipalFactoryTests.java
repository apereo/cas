package org.apereo.cas.authentication.principal;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class SimplePrincipalFactoryTests {
    @Test
    public void checkPrincipalCreation() {
        final PrincipalFactory fact = new DefaultPrincipalFactory();
        final Map<String, Object> map = new HashMap<>();
        map.put("a1", "v1");
        map.put("a2", "v3");

        final Principal p = fact.createPrincipal("user", map);
        assertTrue(p instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
    }

    @Test
    public void checkPrincipalEquality() {
        final PrincipalFactory fact = new DefaultPrincipalFactory();
        final Map<String, Object> map = new HashMap<>();
        map.put("a1", "v1");
        map.put("a2", "v3");

        final Principal p = fact.createPrincipal("user", map);
        final Principal p2 = fact.createPrincipal("USER", map);
        assertTrue(p instanceof SimplePrincipal);
        assertTrue(p2 instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
        assertEquals(p2.getAttributes(), map);
        assertEquals(p2.getAttributes(), p.getAttributes());
        assertEquals(p, p2);
    }
}
