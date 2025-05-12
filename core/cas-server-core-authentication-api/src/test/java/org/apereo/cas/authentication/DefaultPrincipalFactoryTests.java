package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Authentication")
class DefaultPrincipalFactoryTests {
    private static final String UID = "uid";

    @Test
    void checkCreatingSimplePrincipal() throws Throwable {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

    @Test
    void checkCreatingSimplePrincipalWithAttributes() throws Throwable {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID, Map.of("mail", List.of("final@example.com")));
        assertEquals(UID, p.getId());
        assertEquals(1, p.getAttributes().size());
        assertTrue(p.getAttributes().containsKey("mail"));
    }

    @Test
    void checkCreatingSimplePrincipalWithDefaultRepository() throws Throwable {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

    @Test
    void verifyAction() throws Throwable {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertEquals("casuser", p.getId());
        assertEquals(1, p.getAttributes().size());
    }
}
