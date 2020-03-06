package org.apereo.cas.services;

import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.io.WatcherService;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YamlServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("FileSystem")
public class YamlServiceRegistryTests extends AbstractResourceBasedServiceRegistryTests {

    @Override
    @SneakyThrows
    public ServiceRegistry getNewServiceRegistry() {
        dao = new YamlServiceRegistry(RESOURCE,
            WatcherService.noOp(),
            mock(ApplicationEventPublisher.class),
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        return dao;
    }
}
