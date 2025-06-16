package org.apereo.cas.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.MonitoredRepository;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.PullRequest;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MainRepositoryJob {

    private static final int ONE_MINUTE = 60 * 1000;

    private final GitHubOperations gitHub;

    private final MonitoredRepository repository;

    private final List<PullRequestListener> pullRequestListeners;

    @Scheduled(fixedRate = ONE_MINUTE * 3)
    void monitorPullRequests() {
        log.info("Monitoring {}", this.repository.getFullName());
        try {
            log.info("Processing pull requests for {}", this.repository.getFullName());
            var page = this.gitHub.getPullRequests(this.repository.getOrganization(), this.repository.getName());
            while (page != null) {
                page.getContent()
                        .stream()
                        .filter(PullRequest::isOpen)
                        .forEach(pr -> pullRequestListeners.forEach(listener -> {
                            try {
                                listener.onOpenPullRequest(pr);
                            } catch (final Exception e) {
                                log.warn("An error occurred while processing pull request {}", pr.getNumber());
                                log.error(e.getMessage(), e);
                            }
                        }));
                page = page.next();
            }
        } catch (final Exception ex) {
            log.warn("A failure occurred during monitoring", ex);
        }
        log.info("Monitoring of {} completed", this.repository.getFullName());
    }

    @Scheduled(fixedRate = ONE_MINUTE * 5)
    void monitorWorkflowRuns() {
        log.info("Monitoring {}", this.repository.getFullName());
        try {
            log.info("Processing workflow runs for {}", this.repository.getFullName());
            var currentBranches = this.repository.getActiveBranches();
            repository.cancelQualifyingWorkflowRuns(currentBranches);
            repository.removeCancelledWorkflowRuns();
            repository.removePullRequestWorkflowRunsForMissingBranches();
            repository.removeOldWorkflowRuns();
            repository.rerunFailedWorkflowJobs();
        } catch (final Exception ex) {
            log.warn("A failure occurred during monitoring", ex);
        }
        log.info("Monitoring of {} completed", this.repository.getFullName());
    }

}
