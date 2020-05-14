package org.apereo.cas.services;

import org.junit.jupiter.api.Tag;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * This is test cases for {@link InMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("Simple")
public class InMemoryServiceRegistryTests extends AbstractServiceRegistryTests {

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class));
    }
}
