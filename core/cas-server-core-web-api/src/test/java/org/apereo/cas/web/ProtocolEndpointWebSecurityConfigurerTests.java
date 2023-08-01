package org.apereo.cas.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ProtocolEndpointWebSecurityConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
class ProtocolEndpointWebSecurityConfigurerTests {

    @Test
    void verifyOperation() throws Exception {
        val cfg = new ProtocolEndpointWebSecurityConfigurer<>() {
        };
        assertEquals(cfg, cfg.configure(new Object()));
        assertEquals(cfg, cfg.finish(new Object()));
        assertEquals(Ordered.LOWEST_PRECEDENCE, cfg.getOrder());
        assertTrue(cfg.getIgnoredEndpoints().isEmpty());
    }
}
