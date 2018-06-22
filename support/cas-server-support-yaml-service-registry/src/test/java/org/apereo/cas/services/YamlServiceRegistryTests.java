package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YamlServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class YamlServiceRegistryTests extends AbstractResourceBasedServiceRegistryTests {

    @Override
    @SneakyThrows
    public void initializeServiceRegistry() {
        this.dao = new YamlServiceRegistry(RESOURCE, false,
            mock(ApplicationEventPublisher.class),
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy());
        super.initializeServiceRegistry();
    }
}
