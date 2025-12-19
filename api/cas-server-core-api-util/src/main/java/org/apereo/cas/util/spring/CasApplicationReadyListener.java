package org.apereo.cas.util.spring;

import module java.base;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Interface for {@code CasWebApplication} to allow spring {@code @Async} support to use JDK proxy.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
@FunctionalInterface
public interface CasApplicationReadyListener extends CasEventListener {
    /**
     * Handle Application Ready Event.
     *
     * @param event ApplicationReadyEvent fired when application is ready
     */
    @EventListener
    @Async
    void handleApplicationReadyEvent(ApplicationReadyEvent event);
}
