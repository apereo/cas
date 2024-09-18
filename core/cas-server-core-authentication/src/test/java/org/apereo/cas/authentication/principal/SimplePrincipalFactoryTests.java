package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Tag("Authentication")
class SimplePrincipalFactoryTests {
    @Test
    void checkPrincipalCreation() throws Throwable {
        val fact = PrincipalFactoryUtils.newPrincipalFactory();
        val map = new HashMap<String, List<Object>>();
        map.put("a1", List.of("v1"));
        map.put("a2", List.of("v3"));

        val principal = fact.createPrincipal("user", map);
        assertInstanceOf(SimplePrincipal.class, principal);
        assertEquals(principal.getAttributes(), map);
        assertTrue(principal.containsAttribute("a1"));
        assertTrue(principal.containsAttribute("a2"));
        assertEquals("v3", principal.getSingleValuedAttribute("a2"));
        assertThrows(ClassCastException.class, () -> principal.getSingleValuedAttribute("a2", Long.class));
        assertNull(principal.getSingleValuedAttribute("unknown"));
    }

    @Test
    void checkPrincipalEquality() throws Throwable {
        val fact = PrincipalFactoryUtils.newPrincipalFactory();
        val map = new HashMap<String, List<Object>>();
        map.put("a1", List.of("v1"));
        map.put("a2", List.of("v3"));

        val p = fact.createPrincipal("user", map);
        val p2 = fact.createPrincipal("USER", map);
        assertInstanceOf(SimplePrincipal.class, p);
        assertInstanceOf(SimplePrincipal.class, p2);
        assertEquals(p.getAttributes(), map);
        assertEquals(p2.getAttributes(), map);
        assertEquals(p2.getAttributes(), p.getAttributes());
        assertEquals(p, p2);
    }
}
