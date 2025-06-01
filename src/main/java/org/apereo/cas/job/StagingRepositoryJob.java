
package org.apereo.cas.job;

import org.apereo.cas.MonitoredRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class StagingRepositoryJob {

    private static final int ONE_MINUTE = 60 * 1000;

    private final MonitoredRepository repository;

    @Scheduled(fixedRate = ONE_MINUTE * 4)
    void monitorWorkflowRuns() {
        log.info("Monitoring {}", repository.getFullName());
        try {
            var pullRequests = repository.getOpenPullRequests();
            pullRequests.forEach(givenPullRequest -> {
                val pr = repository.getPullRequest(givenPullRequest.getNumber());
                log.info("Processing pull request {}", pr.getNumber());
                repository.autoMergePullRequest(pr);
            });
            log.info("Processing workflow runs for {}", this.repository.getFullName());
            repository.removeCancelledWorkflowRuns();
            repository.removeOldWorkflowRuns();
        } catch (final Exception ex) {
            log.warn("A failure occurred during monitoring", ex);
        }
        log.info("Monitoring of {} completed", this.repository.getFullName());
    }

}
