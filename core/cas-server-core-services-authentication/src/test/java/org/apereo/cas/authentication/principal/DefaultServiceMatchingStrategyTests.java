package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class DefaultServiceMatchingStrategyTests {

    @Test
    public void verifyOperation() {
        val mgr = mock(ServicesManager.class);
        val strategy = new DefaultServiceMatchingStrategy(mgr);
        assertFalse(strategy.matches(null, null));
    }

}
