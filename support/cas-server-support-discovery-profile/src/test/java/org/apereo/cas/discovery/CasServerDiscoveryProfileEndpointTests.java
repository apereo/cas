package org.apereo.cas.discovery;

import org.apereo.cas.web.report.AbstractCasEndpointTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasServerDiscoveryProfileEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.discoveryProfile.enabled=true")
public class CasServerDiscoveryProfileEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("discoveryProfileEndpoint")
    private CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint;

    @Test
    public void verifyOperation() {

    }
}
