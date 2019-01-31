package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractResourceBasedServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("FileSystem")
@RunWith(Parameterized.class)
public abstract class AbstractResourceBasedServiceRegistryTests extends AbstractServiceRegistryTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ServiceRegistry dao;

    public AbstractResourceBasedServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Collections.singletonList(RegexRegisteredService.class);
    }

    @Override
    @SneakyThrows
    public void tearDownServiceRegistry() {
        FileUtils.cleanDirectory(RESOURCE.getFile());
        super.tearDownServiceRegistry();
    }

    @Test
    public void verifyServiceWithInvalidFileName() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setName("hell/o@world:*");
        assertThrows(IllegalArgumentException.class, () -> this.dao.save(r));
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}
