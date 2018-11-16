package org.apereo.cas.prs;

import org.apereo.cas.GitHubProperties;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.GitHubOperations;

/**
 * This is {@link StandardPullRequestListener}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class StandardPullRequestListener implements PullRequestListener {
    private final GitHubOperations gitHub;
    private final GitHubProperties githubProperties;
    private final PullRequestProperties pullRequestProperties;

    public StandardPullRequestListener(final GitHubOperations gitHub, final GitHubProperties githubProperties, final PullRequestProperties pullRequestProperties) {
        this.gitHub = gitHub;
        this.githubProperties = githubProperties;
        this.pullRequestProperties = pullRequestProperties;
    }
}
