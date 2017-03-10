package org.apereo.cas.authentication.principal;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalFactory}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalFactoryTests {

    private static final String UID = "uid";

    @Test
    public void checkCreatingSimplePrincipal() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID);
        assertEquals(p.getId(), UID);
        assertEquals(p.getAttributes().size(), 0);
    }

    @Test
    public void checkCreatingSimplePrincipalWithAttributes() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID, Collections.singletonMap("mail", "final@example.com"));
        assertEquals(p.getId(), UID);
        assertEquals(p.getAttributes().size(), 1);
        assertTrue(p.getAttributes().containsKey("mail"));
    }

    @Test
    public void checkCreatingSimplePrincipalWithDefaultRepository() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID);
        assertEquals(p.getId(), UID);
        assertEquals(p.getAttributes().size(), 0);
    }

}
