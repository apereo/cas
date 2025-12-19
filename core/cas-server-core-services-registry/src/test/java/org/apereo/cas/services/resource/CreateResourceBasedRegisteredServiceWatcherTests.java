package org.apereo.cas.services.resource;

import module java.base;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CreateResourceBasedRegisteredServiceWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RegisteredService")
class CreateResourceBasedRegisteredServiceWatcherTests {

    @Test
    void verifyOperationFoundCreated() {
        val result = new AtomicBoolean(false);
        val mockAppContext = mock(ConfigurableApplicationContext.class);
        doAnswer(args -> {
            val clazz = args.getArgument(0).getClass();
            result.set(clazz.equals(CasRegisteredServiceSavedEvent.class));
            return null;
        }).when(mockAppContext).publishEvent(any());

        val registry = new AbstractResourceBasedServiceRegistry(new ClassPathResource("services"),
            List.of(new RegisteredServiceJsonSerializer(mockAppContext)), mockAppContext,
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
