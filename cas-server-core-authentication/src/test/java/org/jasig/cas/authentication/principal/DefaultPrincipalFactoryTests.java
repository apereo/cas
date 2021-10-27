package org.jasig.cas.authentication.principal;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalFactory}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalFactoryTests {
    @Test
    public void checkCreatingSimplePrincipal() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal("uid");
        assertEquals(p.getId(), "uid");
        assertEquals(p.getAttributes().size(), 0);
    }

    @Test
    public void checkCreatingSimplePrincipalWithAttributes() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal("uid", Collections.<String, Object>singletonMap("mail", "final@example.com"));
        assertEquals(p.getId(), "uid");
        assertEquals(p.getAttributes().size(), 1);
        assertTrue(p.getAttributes().containsKey("mail"));
    }

    @Test
    public void checkCreatingSimplePrincipalWithDefaultRepository() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal("uid");
        assertEquals(p.getId(), "uid");
        assertEquals(p.getAttributes().size(), 0);
    }

}
