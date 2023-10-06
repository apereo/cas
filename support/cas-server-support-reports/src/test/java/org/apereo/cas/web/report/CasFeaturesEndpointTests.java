package org.apereo.cas.web.report;

import org.apereo.cas.configuration.features.CasFeatureModule;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasFeaturesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.casFeatures.enabled=true")
@Tag("ActuatorEndpoint")
class CasFeaturesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casFeaturesEndpoint")
    private CasFeaturesEndpoint endpoint;

    @Test
    void verifyOperation() throws Throwable {
        val features = endpoint.features();
        assertFalse(features.isEmpty());
        assertTrue(CasFeatureModule.FeatureCatalog.Reports.isRegistered());
        assertFalse(CasFeatureModule.FeatureCatalog.Reports.isRegistered("unknown"));
    }
}
