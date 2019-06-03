package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CreateResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CreateResourceBasedRegisteredServiceWatcherTests {

    @Test
    @SneakyThrows
    public void verifyOperationFoundCreated() {
        val result = new AtomicBoolean(false);
        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new RegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServiceSavedEvent.class)),
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
