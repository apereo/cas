package org.apereo.cas.services;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is test cases for {@link InMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Parameterized.class)
public class InMemoryServiceRegistryTests extends AbstractServiceRegistryTests {

    public InMemoryServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return new InMemoryServiceRegistry();
    }
}
