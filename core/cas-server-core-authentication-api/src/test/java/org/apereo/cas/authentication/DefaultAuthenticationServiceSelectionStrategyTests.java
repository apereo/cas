package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class DefaultAuthenticationServiceSelectionStrategyTests {
    @Test
    public void verifyOperation() {
        val strategy = new DefaultAuthenticationServiceSelectionStrategy();
        assertEquals(Ordered.LOWEST_PRECEDENCE, strategy.getOrder());
        assertTrue(strategy.supports(CoreAuthenticationTestUtils.getService()));
        assertNotNull(strategy.resolveServiceFrom(CoreAuthenticationTestUtils.getService()));
    }

}
