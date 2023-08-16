package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ProxyingPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
class ProxyingPrincipalResolverTests {
    @Test
    void verifyOperation() throws Throwable {
        val resolver = new ProxyingPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory());
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertTrue(resolver.supports(credential));
        assertNull(resolver.getAttributeRepository());
        assertNotNull(resolver.resolve(credential));
    }
}
