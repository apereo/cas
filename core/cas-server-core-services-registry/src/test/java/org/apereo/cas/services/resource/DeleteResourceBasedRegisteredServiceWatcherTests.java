package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

import lombok.val;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * This is {@link DeleteResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DeleteResourceBasedRegisteredServiceWatcherTests {

    @Test
    public void verifyOperationNotFound() throws Exception {
        val result = new AtomicBoolean(false);
        val watcher = new DeleteResourceBasedRegisteredServiceWatcher(new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new DefaultRegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServicesLoadedEvent.class))) {
            @Override
            protected String[] getExtensions() {
                return new String[]{"json"};
            }
        });
        watcher.accept(new File("removed.json"));
        assertTrue(result.get());
    }

    @Test
    public void verifyOperationFoundDeleted() throws Exception {
        val result = new AtomicBoolean(false);
        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new DefaultRegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServiceDeletedEvent.class))) {
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
}
