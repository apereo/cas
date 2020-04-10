package org.apereo.cas.web.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasApplicationContextReloadEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.reloadContext.enabled=true")
public class CasApplicationContextReloadEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casApplicationContextReloadEndpoint")
    private CasApplicationContextReloadEndpoint endpoint;

    @Test
    public void verifyOperation() {
        assertNotNull(endpoint);
        assertDoesNotThrow(() -> endpoint.reload());
    }
}
