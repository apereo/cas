package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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

    private ServiceMatchingStrategy strategy;

    @BeforeEach
    public void setup() {
        val mgr = mock(ServicesManager.class);
        this.strategy = new DefaultServiceMatchingStrategy(mgr);
    }

    @Test
    public void verifyServicesAsNull() {
        assertFalse(strategy.matches(null, null));
    }

    @Test
    public void verifyServicesMatch() {
        val service = getService("https://www.google.org");
        assertTrue(strategy.matches(service, service));
    }

    @Test
    public void verifyServicesDifferById() {
        val service1 = getService("https://www.google.org/");
        val service2 = getService("https://www.google.org");
        assertFalse(strategy.matches(service1, service2));
    }

    @Test
    public void verifyServicesDifferByWWW() {
        val service1 = getService("https://google.org");
        val service2 = getService("https://www.google.org");
        assertFalse(strategy.matches(service1, service2));
    }

    @Test
    public void verifyServicesMatchByFragmentEncoded() {
        val service1 = getService("https://google.org");
        val service2 = getService("https://google.org%23/A/B/C");
        assertTrue(strategy.matches(service1, service2));
    }

    @Test
    public void verifyServicesMatchByFragmentDecoded() {
        val service1 = getService("https://google.org");
        val service2 = getService("https://google.org#/A/B/C");
        assertTrue(strategy.matches(service1, service2));
    }

    private static Service getService(final String id) {
        val service = mock(WebApplicationService.class);
        when(service.getId()).thenReturn(id);
        when(service.getOriginalUrl()).thenReturn(id);
        return service;
    }
}
