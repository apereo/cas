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
@Tag("Simple")
public class SimplePrincipalFactoryTests {
    @Test
    public void checkPrincipalCreation() {
        val fact = PrincipalFactoryUtils.newPrincipalFactory();
        val map = new HashMap<String, List<Object>>();
        map.put("a1", List.of("v1"));
        map.put("a2", List.of("v3"));

        val p = fact.createPrincipal("user", map);
        assertTrue(p instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
    }

    @Test
    public void checkPrincipalEquality() {
        val fact = PrincipalFactoryUtils.newPrincipalFactory();
        val map = new HashMap<String, List<Object>>();
        map.put("a1", List.of("v1"));
        map.put("a2", List.of("v3"));

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
