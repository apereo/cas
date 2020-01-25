package org.apereo.cas.web.report;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpringWebflowEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
@TestPropertySource(properties = "management.endpoint.springWebflow.enabled=true")
public class SpringWebflowEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("springWebflowEndpoint")
    private SpringWebflowEndpoint springWebflowEndpoint;

    @Test
    public void verifyOperation() {
        val login = springWebflowEndpoint.getReport("login");
        assertNotNull(login);

        val logout = springWebflowEndpoint.getReport("logout");
        assertNotNull(logout);

        val all = springWebflowEndpoint.getReport(StringUtils.EMPTY);
        assertNotNull(all);
    }
}
