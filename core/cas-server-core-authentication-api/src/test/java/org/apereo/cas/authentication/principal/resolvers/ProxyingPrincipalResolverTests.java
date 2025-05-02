package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Optional;
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
        assertFalse(resolver.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        val credential = new HttpBasedServiceCredential("https://example.org", CoreAuthenticationTestUtils.getRegisteredService());
        var principal = resolver.resolve(credential);
        assertEquals(credential.getId(), principal.getId());
        principal = resolver.resolve(credential,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("helloworld")),
            Optional.empty(), Optional.empty());
        assertEquals("helloworld", principal.getId());
        assertTrue(principal.containsAttribute(HttpBasedServiceCredential.class.getName()));
        assertTrue(credential.getCredentialMetadata().containsProperty(HttpBasedServiceCredential.class.getName()));
    }
}
