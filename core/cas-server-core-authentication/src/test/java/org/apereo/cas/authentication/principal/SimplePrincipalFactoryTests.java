package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class SimplePrincipalFactoryTests {
    @Test
    public void checkPrincipalCreation() {
        val fact = new DefaultPrincipalFactory();
        val map = new HashMap<String, Object>();
        map.put("a1", "v1");
        map.put("a2", "v3");

        val p = fact.createPrincipal("user", map);
        assertTrue(p instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
    }

    @Test
    public void checkPrincipalEquality() {
        val fact = new DefaultPrincipalFactory();
        val map = new HashMap<String, Object>();
        map.put("a1", "v1");
        map.put("a2", "v3");

        val p = fact.createPrincipal("user", map);
        val p2 = fact.createPrincipal("USER", map);
        assertTrue(p instanceof SimplePrincipal);
        assertTrue(p2 instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
        assertEquals(p2.getAttributes(), map);
        assertEquals(p2.getAttributes(), p.getAttributes());
        assertEquals(p, p2);
    }
}
