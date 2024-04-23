package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link ServicesManagerScheduledLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class ServicesManagerScheduledLoader implements Runnable {
    private final ServicesManager servicesManager;

    /**
     * No op runnable.
     *
     * @return the runnable
     */
    public static Runnable noOp() {
        return () -> {
        };
    }

    @Scheduled(
        initialDelayString = "${cas.service-registry.schedule.start-delay:PT20S}",
        fixedDelayString = "${cas.service-registry.schedule.repeat-interval:PT60S}"
    )
    @Override
    public void run() {
        servicesManager.load();
    }
}
