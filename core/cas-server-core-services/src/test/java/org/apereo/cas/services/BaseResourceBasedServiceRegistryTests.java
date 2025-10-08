package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseResourceBasedServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseResourceBasedServiceRegistryTests extends AbstractServiceRegistryTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ResourceBasedServiceRegistry newServiceRegistry;

    @Override
    public void tearDownServiceRegistry() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
        super.tearDownServiceRegistry();
    }

    @Override
    protected ServiceRegistry getNewServiceRegistry() {
        return newServiceRegistry;
    }

    @Test
    void verifyServiceWithInvalidFileName() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type);
            registeredService.setName("hell/o@world:*");
            assertThrows(IllegalArgumentException.class, () -> newServiceRegistry.save(registeredService));
        });
    }

    @Test
    void verifyInvalidFileLoad() {
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
    void verify() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val serializer = mock(StringSerializer.class);
        doThrow(new RuntimeException()).when(serializer).to(any(OutputStream.class), any());
        val registry = new AbstractResourceBasedServiceRegistry(FileUtils.getTempDirectory().toPath(),
            serializer, applicationContext, mock(RegisteredServiceReplicationStrategy.class),
            new DefaultRegisteredServiceResourceNamingStrategy(), List.of(), mock(WatcherService.class)) {
            @Override
            protected String[] getExtensions() {
                return new String[]{".json"};
            }
        };
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class);
        assertThrows(IllegalArgumentException.class, () -> registry.save(r));
        registry.destroy();
    }
}
