package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.Ordered;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreServicesConfiguration.class
})
public class DefaultServicesManagerRegisteredServiceLocatorTests {
    @Autowired
    @Qualifier("defaultServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator defaultServicesManagerRegisteredServiceLocator;

    @Test
    public void verifyDefaultOperation() {
        val input = mock(ServicesManagerRegisteredServiceLocator.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.getName()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());
        assertNotNull(input.getName());
    }

    @Test
    public void verifyOperation() {
        assertNotNull(defaultServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.LOWEST_PRECEDENCE, defaultServicesManagerRegisteredServiceLocator.getOrder());
        val service = RegisteredServiceTestUtils.getRegisteredService("https://example.org.+");
        val result = defaultServicesManagerRegisteredServiceLocator.locate(List.of(service),
            "https://example.org/test",
            r -> r.matches("https://example.org/test"));
        assertNotNull(result);
    }

}
