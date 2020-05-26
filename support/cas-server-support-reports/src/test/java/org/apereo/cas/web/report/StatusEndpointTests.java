package org.apereo.cas.web.report;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StatusEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated since 6.2.0
 */
@TestPropertySource(properties = {
    "management.endpoint.status.enabled=true",
    "management.endpoint.health.enabled=false"
})
@Tag("Simple")
@Deprecated(since ="6.2.0")
public class StatusEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("statusEndpoint")
    private StatusEndpoint statusEndpoint;

    @Test
    public void verifyOperation() {
        val results = statusEndpoint.handle();
        assertFalse(results.isEmpty());
        assertFalse(results.containsKey("health"));
    }
}
