package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcServiceJsonWebKeystoreCacheExpirationPolicyTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val service = getOidcRegisteredService();
        service.setJwksCacheDuration("PT0.005S");
        val policy = new OidcServiceJsonWebKeystoreCacheExpirationPolicy(casProperties);
        assertEquals(5_000_000, policy.expireAfterUpdate(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING),
            Optional.empty(), 1000, 1000));
    }

}
