package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultPrincipalFactoryTests {
    @Test
    public void verifyAction() {
        final var factory = PrincipalFactoryUtils.newPrincipalFactory();
        final var p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertTrue(p.getId().equals("casuser"));
        assertEquals(1, p.getAttributes().size());
    }
}
