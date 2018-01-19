package org.apereo.cas.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@Slf4j
@NoArgsConstructor
public class DomainServicesManagerTests extends AbstractServicesManagerTests {

    @Override
    protected ServicesManager getServicesManagerInstance() {
        return new DomainServicesManager(serviceRegistryDao, mock(ApplicationEventPublisher.class));
    }
}
