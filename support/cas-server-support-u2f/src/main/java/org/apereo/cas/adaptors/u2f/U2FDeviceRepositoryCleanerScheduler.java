package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.util.thread.Cleanable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link U2FDeviceRepositoryCleanerScheduler}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class U2FDeviceRepositoryCleanerScheduler implements Cleanable {
    private final U2FDeviceRepository repository;

    @Scheduled(initialDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.start-delay:PT20S}",
        fixedDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.repeat-interval:PT5M}")
    @Override
    public void clean() {
        LOGGER.debug("Starting to clean expired U2F devices from repository");
        repository.clean();
    }
}
