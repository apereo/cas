package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class DefaultPrincipalFactoryTests {
    private static final String UID = "uid";

    @Test
    public void checkCreatingSimplePrincipal() {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

    @Test
    public void checkCreatingSimplePrincipalWithAttributes() {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID, Collections.singletonMap("mail", List.of("final@example.com")));
        assertEquals(UID, p.getId());
        assertEquals(1, p.getAttributes().size());
        assertTrue(p.getAttributes().containsKey("mail"));
    }

    @Test
    public void checkCreatingSimplePrincipalWithDefaultRepository() {
        val f = PrincipalFactoryUtils.newPrincipalFactory();
        val p = f.createPrincipal(UID);
        assertEquals(UID, p.getId());
        assertTrue(p.getAttributes().isEmpty());
    }

    @Test
    public void verifyAction() {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertTrue(p.getId().equals("casuser"));
        assertEquals(1, p.getAttributes().size());
    }
}
