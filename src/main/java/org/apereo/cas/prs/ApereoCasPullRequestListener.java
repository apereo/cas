package org.apereo.cas.prs;

import org.apereo.cas.MonitoredRepository;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.Milestone;
import org.apereo.cas.github.PullRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ApereoCasPullRequestListener implements PullRequestListener {
    private final MonitoredRepository repository;

    @Override
    public void onOpenPullRequest(final PullRequest pr) {
        log.debug("Processing {}", pr);

        if (!pr.isTargettedAtMasterBranch() && !pr.isLabeledAsSeeMaintenancePolicy()) {
            final Optional<Milestone> milestone = repository.getMilestoneForBranch(pr.getBase().getRef());
            if (!milestone.isPresent()) {
                log.info("{} is targeted at a branch {} that is no longer maintained. See maintenance policy", pr, pr.getBase());
                repository.getGitHub().addLabel(pr, PullRequest.LABEL_SEE_MAINTENANCE_POLICY.getName());
                return;
            }
        }

        if (!pr.getBase().isRefMaster() && !pr.isLabeledAsPendingPortForward()) {
            log.info("{} is targeted at a branch {} and should be ported forward to the master branch in a separate pull request.", pr, pr.getBase());
            repository.getGitHub().addLabel(pr, PullRequest.LABEL_PENDING_PORT_FORWARD.getName());
        }

        if (pr.getMilestone() == null) {
            if (pr.isTargettedAtMasterBranch()) {
                repository.getMilestoneForMaster().ifPresent(milestone -> {
                    log.info("{} will be assigned the master milestone {}", pr, milestone);
                    repository.getGitHub().setMilestone(pr, milestone);
                });
            } else {
                final Optional<Milestone> milestone = repository.getMilestoneForBranch(pr.getBase().getRef());
                milestone.ifPresent(result -> {
                    log.info("{} will be assigned the maintenance milestone {}", pr, milestone);
                    repository.getGitHub().setMilestone(pr, result);
                });
            }
        }
    }
}
