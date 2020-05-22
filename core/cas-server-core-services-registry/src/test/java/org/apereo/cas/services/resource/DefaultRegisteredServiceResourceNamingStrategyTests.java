package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRegisteredServiceResourceNamingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultRegisteredServiceResourceNamingStrategyTests {
    @Test
    public void verifyOperation() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(1000L);
        when(service.getName()).thenReturn("Test");

        val strategy = new DefaultRegisteredServiceResourceNamingStrategy();
        assertNotNull(strategy.buildNamingPattern("json", "xml", "xyz"));
        val result = strategy.build(service, "json");
        assertNotNull(result);
        assertEquals("Test-1000.json", result);
    }
}
