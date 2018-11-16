package org.apereo.cas.prs;

import org.apereo.cas.GitHubProperties;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.PullRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApereoCasPullRequestListener implements PullRequestListener {
    private final GitHubOperations gitHub;
    private final GitHubProperties githubProperties;

    public ApereoCasPullRequestListener(final GitHubOperations gitHub, final GitHubProperties githubProperties) {
        this.gitHub = gitHub;
        this.githubProperties = githubProperties;
    }

    @Override
    public void onOpenPullRequest(final PullRequest pr) {
        final PullRequestProperties properties = githubProperties.getPrs();
        log.debug("Processing {}", pr);

        if (!properties.getMaintainedBranches().contains(pr.getBase().getRef()) && !pr.isLabeledAsSeeMaintenancePolicy()) {
            log.warn("{} is targeted at a branch {} that is no longer maintained. See maintenance policy", pr, pr.getBase());
        }

    }
}
