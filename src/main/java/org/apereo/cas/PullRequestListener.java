package org.apereo.cas;

import org.apereo.cas.github.PullRequest;

@FunctionalInterface
public interface PullRequestListener {
    void onOpenPullRequest(PullRequest pr);
}
