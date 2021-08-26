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
public class OidcServiceJsonWebKeystoreCacheExpirationPolicyTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val service = getOidcRegisteredService();
        service.setJwksCacheDuration(5);
        service.setJwksCacheTimeUnit("milliseconds");
        val policy = new OidcServiceJsonWebKeystoreCacheExpirationPolicy(casProperties);
        assertEquals(5_000_000, policy.expireAfterUpdate(service, Optional.empty(), 1000, 1000));
    }

}
