package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;

/**
 * This is test cases for {@link InMemoryServiceRegistry}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class InMemoryServiceRegistryTests extends AbstractServiceRegistryTests {

    private static final String SERVICE_ID = "service";

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return new InMemoryServiceRegistry();
    }

}
