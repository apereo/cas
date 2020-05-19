package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ModifyResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Simple")
public class ModifyResourceBasedRegisteredServiceWatcherTests {

    @Test
    @SneakyThrows
    public void verifyOperationFoundModified() {
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
        val service = registry.findServiceById(1);
        service.setEvaluationOrder(666);
        registry.load();
        val temp = new FileSystemResource(File.createTempFile("Sample-1", ".json"));
        new RegisteredServiceJsonSerializer().to(temp.getFile(), service);

        val watcher = new ModifyResourceBasedRegisteredServiceWatcher(registry);
        watcher.accept(temp.getFile());
        assertTrue(result.get());
        assertEquals(1, registry.size());
        registry.removeRegisteredService(service);
        assertEquals(0, registry.size());
    }
}
