package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceMetadataHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2Web")
@Execution(ExecutionMode.SAME_THREAD)
class SamlRegisteredServiceMetadataHealthIndicatorTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlRegisteredServiceMetadataHealthIndicator")
    private HealthIndicator samlRegisteredServiceMetadataHealthIndicator;

    @BeforeEach
    public void setup() {
        this.servicesManager.deleteAll();
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(samlRegisteredServiceMetadataHealthIndicator);
        servicesManager.save(SamlIdPTestUtils.getSamlRegisteredService());
        val health = samlRegisteredServiceMetadataHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        samlRegisteredService.setMetadataLocation("unknown-metadata-location");
        servicesManager.save(samlRegisteredService);
        val health = samlRegisteredServiceMetadataHealthIndicator.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void verifyFailsOperationWithMultiple() throws Throwable {
        val samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService(UUID.randomUUID().toString());
        samlRegisteredService.setMetadataLocation("unknown-metadata-location");
        servicesManager.save(samlRegisteredService);
        servicesManager.save(SamlIdPTestUtils.getSamlRegisteredService());
        val health = samlRegisteredServiceMetadataHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
    }

}
