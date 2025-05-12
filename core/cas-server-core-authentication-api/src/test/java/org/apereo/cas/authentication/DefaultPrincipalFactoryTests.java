package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val principal = factory.createPrincipal(UID);
        assertEquals(UID, principal.getId());
        assertTrue(principal.getAttributes().isEmpty());
    }

    @Test
    void checkCreatingSimplePrincipalWithAttributes() throws Throwable {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val principal = factory.createPrincipal(UID, Map.of("mail", List.of("final@example.com")));
        assertEquals(UID, principal.getId());
        assertEquals(1, principal.getAttributes().size());
        assertTrue(principal.getAttributes().containsKey("mail"));
    }

    @Test
    void checkCreatingSimplePrincipalWithDefaultRepository() throws Throwable {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val principal = factory.createPrincipal(UID);
        assertEquals(UID, principal.getId());
        assertTrue(principal.getAttributes().isEmpty());
    }

    @Test
    void verifyAction() throws Throwable {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val principal = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertEquals("casuser", principal.getId());
        assertEquals(1, principal.getAttributes().size());
    }
}
