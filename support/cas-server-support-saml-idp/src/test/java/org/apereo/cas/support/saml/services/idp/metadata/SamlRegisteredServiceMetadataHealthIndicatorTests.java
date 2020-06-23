package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceMetadataHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
public class SamlRegisteredServiceMetadataHealthIndicatorTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlRegisteredServiceMetadataHealthIndicator")
    private HealthIndicator samlRegisteredServiceMetadataHealthIndicator;

    @Test
    public void verifyOperation() {
        assertNotNull(samlRegisteredServiceMetadataHealthIndicator);
        servicesManager.save(SamlIdPTestUtils.getSamlRegisteredService());
        val health = samlRegisteredServiceMetadataHealthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
    }

}
