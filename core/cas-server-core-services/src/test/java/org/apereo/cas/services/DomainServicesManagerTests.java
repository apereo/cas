package org.apereo.cas.services;

import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class DomainServicesManagerTests extends AbstractServicesManagerTests {
    public DomainServicesManagerTests() {
        super();
    }

    @Override
    protected ServicesManager getServicesManagerInstance() {
        return new DomainServicesManager(serviceRegistryDao, mock(ApplicationEventPublisher.class));
    }
}
