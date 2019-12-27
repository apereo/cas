package org.apereo.cas.web.report;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasResolveAttributesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "management.endpoint.resolveAttributes.enabled=true",
    "spring.mail.host=localhost",
    "spring.mail.port=25000"
})
public class CasResolveAttributesReportEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("resolveAttributesReportEndpoint")
    private CasResolveAttributesReportEndpoint endpoint;

    @Test
    public void verifyOperation() {
        val response = endpoint.resolvePrincipalAttributes("casuser");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}

