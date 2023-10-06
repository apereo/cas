package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EchoingPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Attributes")
class EchoingPrincipalResolverTests {
    @Test
    void verifyOperation() throws Throwable {
        val input = new EchoingPrincipalResolver();
        assertNull(input.getAttributeRepository());
        assertTrue(input.supports(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));

    }

}
