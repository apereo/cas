package org.apereo.cas.authentication.principal;

import module java.base;
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
class DefaultServiceMatchingStrategyTests {

    private ServiceMatchingStrategy strategy;

    @BeforeEach
    void setup() {
        val mgr = mock(ServicesManager.class);
        this.strategy = new DefaultServiceMatchingStrategy(mgr);
    }

    @Test
    void verifyServicesAsNull() {
        assertFalse(strategy.matches(null, null));
    }

    @Test
    void verifyServicesMatch() {
        val service = getService("https://www.google.org");
        assertTrue(strategy.matches(service, service));
    }

    @Test
    void verifyServicesDifferById() {
        val service1 = getService("https://www.google.org/");
        val service2 = getService("https://www.google.org");
        assertFalse(strategy.matches(service1, service2));
    }

    @Test
    void verifyServicesDifferByWWW() {
        val service1 = getService("https://google.org");
        val service2 = getService("https://www.google.org");
        assertFalse(strategy.matches(service1, service2));
    }

    @Test
    void verifyServicesDifferByEncodedFragment() {
        val service1 = getService("https://google.org");
        val service2 = getService("https://google.org%23/A/B/C");
        assertFalse(strategy.matches(service1, service2));
    }

    @Test
    void verifyServicesDifferByEncodedFragmentInUserInfo() {
        val service1 = getService("https://trusted-service.domain.local");
        val service2 = getService("https://trusted-service.domain.local%23@attacker.example/capture");
        assertFalse(strategy.matches(service1, service2));
        assertFalse(strategy.matches(service2, service1));
    }

    @Test
    void verifyServicesMatchByQueryEncoding() {
        val service1 = getService("https://google.org/app?param=hello+world");
        val service2 = getService("https://google.org/app?param=hello%20world");
        assertTrue(strategy.matches(service1, service2));
        assertTrue(strategy.matches(service2, service1));
        assertFalse(strategy.matches(getService("https://google.org%2Fapp?param=hello"), getService("https://google.org/app?param=hello")));
    }

    @Test
    void verifyServicesMatchByFragmentDecoded() {
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
