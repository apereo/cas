package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
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

    @Test
    void verifyEachLocationIsProbedOncePerCheck() {
        val metadataResolver = mock(SamlRegisteredServiceMetadataResolver.class);
        when(metadataResolver.supports(any())).thenReturn(Boolean.TRUE);
        when(metadataResolver.isAvailable(any())).thenReturn(Boolean.TRUE);
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        plan.registerMetadataResolver(metadataResolver);

        val services = new ArrayList<RegisteredService>();
        for (var i = 0; i < 5; i++) {
            val service = SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString());
            service.setId(i);
            service.setName("Service-" + i);
            service.setMetadataLocation(i < 4 ? "jdbc://" : "mongodb://");
            services.add(service);
        }
        val isolatedServicesManager = mock(ServicesManager.class);
        when(isolatedServicesManager.findServiceBy(any(Predicate.class))).thenReturn(services);
        val health = new SamlRegisteredServiceMetadataHealthIndicator(plan, isolatedServicesManager).health();
        assertEquals(Status.UP, health.getStatus());
        verify(metadataResolver, times(2)).isAvailable(any());
    }

    @Test
    void verifyStaleMetadataIsReported() {
        val metadataResolver = mock(SamlRegisteredServiceMetadataResolver.class);
        when(metadataResolver.supports(any())).thenReturn(Boolean.TRUE);
        when(metadataResolver.isAvailable(any())).thenReturn(Boolean.TRUE);
        when(metadataResolver.describe(any(), any())).thenReturn(Map.of("metadataBackupStale", Boolean.TRUE, "metadataBackupAge", "P9D"));
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        plan.registerMetadataResolver(metadataResolver);

        val service = SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString());
        val isolatedServicesManager = mock(ServicesManager.class);
        when(isolatedServicesManager.findServiceBy(any(Predicate.class))).thenReturn(List.of(service));
        val health = new SamlRegisteredServiceMetadataHealthIndicator(plan, isolatedServicesManager).health();
        assertEquals(Status.UP, health.getStatus());
        val details = (Map<String, Object>) health.getDetails().get(service.getName());
        assertEquals(Boolean.TRUE, details.get("metadataStale"));
        assertEquals("P9D", details.get("metadataBackupAge"));
    }

    private SamlRegisteredServiceMetadataHealthIndicator healthIndicatorFor(final RegisteredService... services) {
        val isolatedServicesManager = mock(ServicesManager.class);
        when(isolatedServicesManager.findServiceBy(any(Predicate.class))).thenReturn(List.of(services));
        return new SamlRegisteredServiceMetadataHealthIndicator(
            samlRegisteredServiceMetadataResolvers, isolatedServicesManager);
    }
}
