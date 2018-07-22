package org.apereo.cas.services;

import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;

import lombok.SneakyThrows;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link YamlServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Parameterized.class)
public class YamlServiceRegistryTests extends AbstractResourceBasedServiceRegistryTests {

    public YamlServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }

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
