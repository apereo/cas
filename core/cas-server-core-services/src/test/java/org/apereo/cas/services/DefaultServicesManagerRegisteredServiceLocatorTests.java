package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.Serial;
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
    WebMvcAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreServicesConfiguration.class
})
class DefaultServicesManagerRegisteredServiceLocatorTests {
    @Autowired
    @Qualifier("defaultServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator defaultServicesManagerRegisteredServiceLocator;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Test
    void verifyDefaultOperation() throws Throwable {
        val input = mock(ServicesManagerRegisteredServiceLocator.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.getRegisteredServiceIndexes()).thenCallRealMethod();
        when(input.getName()).thenCallRealMethod();
        assertEquals(ServicesManagerRegisteredServiceLocator.DEFAULT_ORDER, input.getOrder());
        assertNotNull(input.getName());
        assertTrue(input.getRegisteredServiceIndexes().isEmpty());
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(defaultServicesManagerRegisteredServiceLocator);
        assertEquals(ServicesManagerRegisteredServiceLocator.DEFAULT_ORDER, defaultServicesManagerRegisteredServiceLocator.getOrder());
        val service = RegisteredServiceTestUtils.getRegisteredService("https://example.org.+");
        val result = defaultServicesManagerRegisteredServiceLocator.locate(List.of(service),
            webApplicationServiceFactory.createService("https://example.org/test"));
        assertNotNull(result);
        assertFalse(defaultServicesManagerRegisteredServiceLocator.getRegisteredServiceIndexes().isEmpty());
    }

    @Test
    void verifyExtendedServices() throws Throwable {
        val service = new ExtendedRegisteredService();
        service.setServiceId("https://\\w+.org.+");
        service.setId(100);
        val result = defaultServicesManagerRegisteredServiceLocator.locate(List.of(service),
            webApplicationServiceFactory.createService("https://example.org/test"));
        assertNotNull(result);
    }

    @Test
    void verifyUnmatchedExtendedServices() throws Throwable {
        val service = new ExtendedRegisteredService() {
            @Serial
            private static final long serialVersionUID = 3435937253967470900L;

            @Override
            public String getFriendlyName() {
                return "OtherService";
            }
        };
        service.setServiceId("https://\\w+.org.+");
        service.setId(100);
        val result = defaultServicesManagerRegisteredServiceLocator.locate(List.of(service),
            webApplicationServiceFactory.createService("https://example.org/test"));
        assertNull(result);
    }


    private static class ExtendedRegisteredService extends CasRegisteredService {
        @Serial
        private static final long serialVersionUID = 1820837947166559349L;
    }
}
