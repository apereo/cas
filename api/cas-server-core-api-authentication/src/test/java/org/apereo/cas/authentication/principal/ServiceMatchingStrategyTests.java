package org.apereo.cas.authentication.principal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class ServiceMatchingStrategyTests {
    @Test
    public void verifyOperation() {
        assertTrue(ServiceMatchingStrategy.alwaysMatches()
            .matches(mock(Service.class), mock(Service.class)));
        assertFalse(ServiceMatchingStrategy.neverMatches()
            .matches(mock(Service.class), mock(Service.class)));
    }
}
