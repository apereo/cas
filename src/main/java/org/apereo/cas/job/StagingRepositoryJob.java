
package org.apereo.cas.job;

import org.apereo.cas.MonitoredRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class StagingRepositoryJob {

    private static final int ONE_MINUTE = 60 * 1000;

    private final MonitoredRepository repository;

    @Scheduled(fixedRate = ONE_MINUTE * 2)
    void monitorWorkflowRuns() {
        log.info("Monitoring {}", this.repository.getFullName());
        try {
            log.info("Processing workflow runs for {}", this.repository.getFullName());
            repository.removeCancelledWorkflowRuns();
            repository.removeOldWorkflowRuns();
        } catch (final Exception ex) {
            log.warn("A failure occurred during monitoring", ex);
        }
        log.info("Monitoring of {} completed", this.repository.getFullName());
    }

}
