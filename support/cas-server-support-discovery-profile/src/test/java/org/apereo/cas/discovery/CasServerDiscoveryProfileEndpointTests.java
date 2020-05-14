package org.apereo.cas.discovery;

import org.apereo.cas.config.CasDiscoveryProfileConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServerDiscoveryProfileEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    CasDiscoveryProfileConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.discoveryProfile.enabled=true"
    })
@Tag("Simple")
public class CasServerDiscoveryProfileEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("discoveryProfileEndpoint")
    private CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint;

    @Test
    public void verifyOperation() {
        val discovery = discoveryProfileEndpoint.discovery();
        assertNotNull(discovery);
        assertFalse(discovery.isEmpty());
    }
}
