package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link ServicesManagerScheduledLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
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
        initialDelayString = "${cas.serviceRegistry.schedule.startDelay:20000}",
        fixedDelayString = "${cas.serviceRegistry.schedule.repeatInterval:60000}"
    )
    @Override
    public void run() {
        servicesManager.load();
    }
}
