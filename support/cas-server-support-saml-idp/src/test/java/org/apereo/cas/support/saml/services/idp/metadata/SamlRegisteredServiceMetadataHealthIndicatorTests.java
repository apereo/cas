package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceMetadataHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2Web")
class SamlRegisteredServiceMetadataHealthIndicatorTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlRegisteredServiceMetadataHealthIndicator")
    private HealthIndicator samlRegisteredServiceMetadataHealthIndicator;

    @Autowired
    @Qualifier("samlRegisteredServiceMetadataResolvers")
    private SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers;

    @Test
    void verifyOperation() {
        assertNotNull(samlRegisteredServiceMetadataHealthIndicator);
        assertNotNull(samlRegisteredServiceMetadataHealthIndicator.health());
        val health = healthIndicatorFor(SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString())).health();
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void verifyFailsOperation() {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString());
        samlRegisteredService.setMetadataLocation("unknown-metadata-location");

        val health = healthIndicatorFor(samlRegisteredService).health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void verifyFailsOperationWithMultiple() {
        val unresolvableService = SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString());
        unresolvableService.setMetadataLocation("unknown-metadata-location");
        val health = healthIndicatorFor(unresolvableService,
            SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString())).health();
        assertEquals(Status.UP, health.getStatus());
    }

    private SamlRegisteredServiceMetadataHealthIndicator healthIndicatorFor(final RegisteredService... services) {
        val isolatedServicesManager = mock(ServicesManager.class);
        when(isolatedServicesManager.findServiceBy(any(Predicate.class))).thenReturn(List.of(services));
        return new SamlRegisteredServiceMetadataHealthIndicator(
            samlRegisteredServiceMetadataResolvers, isolatedServicesManager);
    }
}
