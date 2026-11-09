package org.apereo.cas.oidc.federation.subordinate;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcFederationSubordinateRepositoryTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("OIDCServices")
class OidcFederationSubordinateRepositoryTests extends AbstractOidcTrustAnchorFederationTests {

    @Autowired
    @Qualifier("oidcFederationSubordinateRepository")
    private OidcFederationSubordinateRepository oidcFederationSubordinateRepository;

    @Test
    public void verifyOperation() {
        assertEquals(5, oidcFederationSubordinateRepository.getSubordinates().size());
        assertInstanceOf(ConcurrentMap.class, oidcFederationSubordinateRepository.getSubordinates());
    }

    /**
     * Subordinates are read and added concurrently by the federation endpoints, so a definition without an entity
     * identifier, which cannot be a key, is ignored instead of failing the others.
     */
    @Test
    void verifySubordinateWithoutEntityIdIsIgnored() throws Exception {
        val directory = Files.createTempDirectory("subordinates");
        Files.writeString(directory.resolve("named.json"), """
            {"entityId": "https://named.example.org", "federationKeys": []}
            """);
        Files.writeString(directory.resolve("unnamed.json"), """
            {"federationKeys": []}
            """);
        val properties = new OidcProperties();
        properties.getFederation().setSubordinateDirectory(directory.toString());
        val repository = new OidcFederationSubordinateRepository(properties);
        assertEquals(Set.of("https://named.example.org"), repository.getSubordinates().keySet());
    }
}
