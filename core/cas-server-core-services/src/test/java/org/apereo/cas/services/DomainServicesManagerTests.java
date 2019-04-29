package org.apereo.cas.services;

import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;
import org.apereo.cas.services.domain.DomainServicesManager;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@NoArgsConstructor
public class DomainServicesManagerTests extends AbstractServicesManagerTests {

    @Override
    protected ServicesManager getServicesManagerInstance() {
        return new DomainServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class),
            new DefaultRegisteredServiceDomainExtractor(),
            new HashSet<>());
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
        this.servicesManager.save(r);

        this.servicesManager.deleteAll();
        assertTrue(this.servicesManager.getDomains().isEmpty());
    }
}
