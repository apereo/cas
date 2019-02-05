package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultPrincipalFactoryTests {
    @Test
    public void verifyAction() {
        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertTrue(p.getId().equals("casuser"));
        assertEquals(1, p.getAttributes().size());
    }
}
