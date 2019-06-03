package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

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
 * This is {@link DeleteResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DeleteResourceBasedRegisteredServiceWatcherTests {

    @Test
    @SneakyThrows
    public void verifyOperationNotFound() {
        val result = new AtomicBoolean(false);
        val watcher = new DeleteResourceBasedRegisteredServiceWatcher(new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new RegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServicesLoadedEvent.class)),
            new ArrayList<>()) {
            @Override
            protected String[] getExtensions() {
                return new String[]{"json"};
            }
        });
        watcher.accept(new File("removed.json"));
        assertTrue(result.get());
    }

    @Test
    @SneakyThrows
    public void verifyOperationFoundDeleted() {
        val result = new AtomicBoolean(false);
        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new RegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServiceDeletedEvent.class)),
            new ArrayList<>()) {
            @Override
            protected String[] getExtensions() {
                return new String[]{"json"};
            }
        };
        var results = registry.load();
        assertFalse(results.isEmpty());
        val watcher = new DeleteResourceBasedRegisteredServiceWatcher(registry);
        watcher.accept(new File("Sample-1.json"));
        assertTrue(result.get());
        assertEquals(0, registry.size());
    }

    @Test
    public void verifyTempFilesIgnored() throws Exception {
        val result = new AtomicBoolean(false);
        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
                Collections.singletonList(new RegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServiceDeletedEvent.class)),
            new ArrayList<>()) {
            @Override
            protected String[] getExtensions() {
                return new String[]{"json"};
            }
        };
        var results = registry.load();
        assertFalse(results.isEmpty());
        val watcher = new DeleteResourceBasedRegisteredServiceWatcher(registry);
        watcher.accept(new File(".Sample-1.json"));
        assertFalse(result.get());
        watcher.accept(new File("Sample-1.json.swp"));
        assertFalse(result.get());
        assertEquals(1, registry.size());
    }
}
