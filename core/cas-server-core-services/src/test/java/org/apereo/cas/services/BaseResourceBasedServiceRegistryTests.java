package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseResourceBasedServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public abstract class BaseResourceBasedServiceRegistryTests extends AbstractServiceRegistryTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ResourceBasedServiceRegistry newServiceRegistry;

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
        assertThrows(IllegalArgumentException.class, () -> this.newServiceRegistry.save(r));
    }

    @Test
    public void verifyInvalidFileLoad() {
        val file = mock(File.class);
        when(file.canRead()).thenReturn(Boolean.FALSE);
        assertTrue(newServiceRegistry.load(file).isEmpty());

        when(file.exists()).thenReturn(Boolean.FALSE);
        assertTrue(newServiceRegistry.load(file).isEmpty());

        when(file.length()).thenReturn(0L);
        assertTrue(newServiceRegistry.load(file).isEmpty());

        when(file.getName()).thenReturn(".ignore");
        assertTrue(newServiceRegistry.load(file).isEmpty());

        when(file.getName()).thenReturn("file.ignore");
        assertTrue(newServiceRegistry.load(file).isEmpty());
    }

    @Test
    public void verify() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val serializer = mock(StringSerializer.class);
        doThrow(new RuntimeException()).when(serializer).to(any(OutputStream.class), any());

        val registry = new AbstractResourceBasedServiceRegistry(FileUtils.getTempDirectory().toPath(), 
            serializer, applicationContext, mock(RegisteredServiceReplicationStrategy.class),
            new DefaultRegisteredServiceResourceNamingStrategy(), List.of(), mock(WatcherService.class)) {
            @Override
            protected String[] getExtensions() {
                return new String[] {".json"};
            }
        };
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), RegexRegisteredService.class);
        assertThrows(IllegalArgumentException.class, () -> registry.save(r));
        registry.destroy();
    }
}
