package org.apereo.cas.services;

import org.apereo.cas.services.domain.DefaultDomainAwareServicesManager;
import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@NoArgsConstructor
@Tag("Simple")
public class DefaultDomainAwareServicesManagerTests extends AbstractServicesManagerTests {
    private DefaultDomainAwareServicesManager defaultDomainAwareServicesManager;

    @Override
    protected ServicesManager getServicesManagerInstance() {
        defaultDomainAwareServicesManager = new DefaultDomainAwareServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class),
            new DefaultRegisteredServiceDomainExtractor(),
            new HashSet<>());
        return defaultDomainAwareServicesManager;
    }

    @Test
    public void verifyDeleteEmptyDomains() {
        var r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        this.servicesManager.save(r);

        r = new RegexRegisteredService();
        r.setId(20);
        r.setName("domainService2");
        r.setServiceId("https://www.example.com/two");
        servicesManager.save(r);

        servicesManager.deleteAll();

        assertTrue(this.defaultDomainAwareServicesManager.getDomains().collect(Collectors.toList()).isEmpty());
    }
}
