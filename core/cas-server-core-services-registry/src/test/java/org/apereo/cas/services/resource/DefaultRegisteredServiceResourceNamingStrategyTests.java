package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRegisteredServiceResourceNamingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
public class DefaultRegisteredServiceResourceNamingStrategyTests {
    @Test
    public void verifyOperation() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(1000L);
        when(service.getName()).thenReturn("Test");

        val strategy = new DefaultRegisteredServiceResourceNamingStrategy();
        val pattern = strategy.buildNamingPattern("json", "xml", "xyz");
        assertNotNull(pattern);
        val result = strategy.build(service, "json");
        assertNotNull(result);
        assertEquals("Test-1000.json", result);
        verifyFileNamePattern(pattern, "Test-1000.json");
        verifyFileNamePattern(pattern, "Test-1000.json");
        verifyFileNamePattern(pattern, "Test-Example-1000.json");
        verifyFileNamePattern(pattern, "Multiple-Test-Example-1.xml");
    }

    private static void verifyFileNamePattern(final Pattern pattern, final String name) {
        val matcher = pattern.matcher(name);
        assertTrue(matcher.find());
        assertNotNull(matcher.group(1));
        assertNotNull(matcher.group(2));
    }
}
