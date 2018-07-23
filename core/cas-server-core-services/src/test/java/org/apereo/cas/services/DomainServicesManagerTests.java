package org.apereo.cas.services;

import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@NoArgsConstructor
public class DomainServicesManagerTests extends AbstractServicesManagerTests {

    @Override
    protected ServicesManager getServicesManagerInstance() {
        return new DomainServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class));
    }
}
