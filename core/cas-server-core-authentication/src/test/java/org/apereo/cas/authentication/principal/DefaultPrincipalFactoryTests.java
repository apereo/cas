package org.apereo.cas.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalFactory}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class DefaultPrincipalFactoryTests {

    private static final String UID = "uid";

    @Test
    public void checkCreatingSimplePrincipal() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

    @Test
    public void checkCreatingSimplePrincipalWithAttributes() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID, Collections.singletonMap("mail", "final@example.com"));
        assertEquals(UID, p.getId());
        assertEquals(1, p.getAttributes().size());
        assertTrue(p.getAttributes().containsKey("mail"));
    }

    @Test
    public void checkCreatingSimplePrincipalWithDefaultRepository() {
        final PrincipalFactory f = new DefaultPrincipalFactory();
        final Principal p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

}
