package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CreateResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Simple")
public class CreateResourceBasedRegisteredServiceWatcherTests {

    @Test
    @SneakyThrows
    public void verifyOperationFoundCreated() {
        val result = new AtomicBoolean(false);
        val mockAppContext = mock(ConfigurableApplicationContext.class);
        doAnswer(args -> {
            val clazz = args.getArgument(0).getClass();
            result.set(clazz.equals(CasRegisteredServiceSavedEvent.class));
            return null;
        }).when(mockAppContext).publishEvent(any());

        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            List.of(new RegisteredServiceJsonSerializer()), mockAppContext,
            new ArrayList<>()) {
            @Override
            protected String[] getExtensions() {
                return new String[]{"json"};
            }
        };
        var results = registry.load();
        assertFalse(results.isEmpty());
        val watcher = new CreateResourceBasedRegisteredServiceWatcher(registry);
        watcher.accept(new File(registry.getServiceRegistryDirectory().toFile(), "Sample-1.json"));
        assertTrue(result.get());
        assertEquals(1, registry.size());
    }
}
