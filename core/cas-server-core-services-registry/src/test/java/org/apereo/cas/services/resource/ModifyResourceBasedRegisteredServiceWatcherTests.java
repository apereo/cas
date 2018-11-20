package org.apereo.cas.services.resource;

import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.val;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * This is {@link ModifyResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ModifyResourceBasedRegisteredServiceWatcherTests {

    @Test
    public void verifyOperationFoundModified() throws Exception {
        val result = new AtomicBoolean(false);
        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            Collections.singletonList(new DefaultRegisteredServiceJsonSerializer()), o -> result.set(o.getClass().equals(CasRegisteredServiceSavedEvent.class))) {
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
        new DefaultRegisteredServiceJsonSerializer().to(temp.getFile(), service);

        val watcher = new ModifyResourceBasedRegisteredServiceWatcher(registry);
        watcher.accept(temp.getFile());
        assertTrue(result.get());
        assertEquals(1, registry.size());
        registry.removeRegisteredService(service);
        assertEquals(0, registry.size());
    }
}
