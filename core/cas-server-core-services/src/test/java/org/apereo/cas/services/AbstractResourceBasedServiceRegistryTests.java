package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractResourceBasedServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("FileSystem")
public abstract class AbstractResourceBasedServiceRegistryTests extends AbstractServiceRegistryTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ResourceBasedServiceRegistry dao;

    public static Stream<Class<? extends RegisteredService>> getParameters() {
        return AbstractServiceRegistryTests.getParameters();
    }

    @Override
    @SneakyThrows
    public void tearDownServiceRegistry() {
        FileUtils.cleanDirectory(RESOURCE.getFile());
        super.tearDownServiceRegistry();
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyServiceWithInvalidFileName(final Class<? extends RegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setName("hell/o@world:*");
        assertThrows(IllegalArgumentException.class, () -> this.dao.save(r));
    }

    @Test
    public void verifyInvalidFileLoad() {
        val file = mock(File.class);
        when(file.canRead()).thenReturn(Boolean.FALSE);
        assertTrue(dao.load(file).isEmpty());

        when(file.exists()).thenReturn(Boolean.FALSE);
        assertTrue(dao.load(file).isEmpty());

        when(file.length()).thenReturn(0L);
        assertTrue(dao.load(file).isEmpty());

        when(file.getName()).thenReturn(".ignore");
        assertTrue(dao.load(file).isEmpty());

        when(file.getName()).thenReturn("file.ignore");
        assertTrue(dao.load(file).isEmpty());
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}
