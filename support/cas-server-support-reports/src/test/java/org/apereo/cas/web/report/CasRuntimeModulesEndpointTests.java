package org.apereo.cas.web.report;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasRuntimeModulesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.casModules.enabled=true")
@Tag("ActuatorEndpoint")
class CasRuntimeModulesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casRuntimeModulesEndpoint")
    private CasRuntimeModulesEndpoint endpoint;

    @Test
    void verifyOperation() throws Throwable {
        var modules = endpoint.reportModules();
        assertFalse(modules.isEmpty());

        val module = modules.getFirst();
        assertNotNull(module.getName());
        assertNotNull(module.getDescription());
        assertNotNull(module.getVersion());
    }
}
