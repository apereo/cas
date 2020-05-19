package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.context.support.StaticApplicationContext;

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
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        return new InMemoryServiceRegistry(appCtx);
    }
}
