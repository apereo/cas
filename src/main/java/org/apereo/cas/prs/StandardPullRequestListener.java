package org.apereo.cas.prs;

import org.apereo.cas.GitHubProperties;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.PullRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandardPullRequestListener implements PullRequestListener {
    private final GitHubOperations gitHub;
    private final GitHubProperties githubProperties;
    private final PullRequestProperties pullRequestProperties;

    public StandardPullRequestListener(final GitHubOperations gitHub, final GitHubProperties githubProperties, final PullRequestProperties pullRequestProperties) {
        this.gitHub = gitHub;
        this.githubProperties = githubProperties;
        this.pullRequestProperties = pullRequestProperties;
    }

    @Override
    public void onOpenPullRequest(final PullRequest pr) {
        log.info(pr.getTitle());
    }
}
