package org.jasig.cas.authentication.principal;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepositoryTests {
    private final PrincipalFactory factory = new DefaultPrincipalFactory();

    @Test
    public void checkNoAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(this.factory.createPrincipal("uid")).size(), 0);
    }

    @Test
    public void checkInitialAttributes() {
        final Principal p = this.factory.createPrincipal("uid",
                Collections.<String, Object>singletonMap("mail", "final@example.com"));
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(p).size(), 1);
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }
}
