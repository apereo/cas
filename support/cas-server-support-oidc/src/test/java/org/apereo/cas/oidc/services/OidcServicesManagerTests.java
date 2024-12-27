package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("OIDCServices")
@TestPropertySource(properties = "cas.service-registry.core.index-services=true")
class OidcServicesManagerTests extends AbstractOidcTests {

    @Test
    void verifyParallelLoading() {
        for (var i = 0; i < 250; i++) {
            val redirectUri = "https://cas.oidc.org/%s".formatted(RandomUtils.randomAlphabetic(8));
            val oidcService = getOidcRegisteredService(UUID.randomUUID().toString(), redirectUri).setId(i);
            val casService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()).setId(i);
            val oauthService = getOAuthRegisteredService(UUID.randomUUID().toString(), redirectUri).setId(i);
            servicesManager.save(oauthService, oidcService, casService);
        }
        IntStream.range(0, 100).parallel().forEach(__ ->
            assertFalse(servicesManager.getAllServices().isEmpty()));
    }
}
