package org.apereo.cas;

import org.apereo.cas.github.PullRequest;

/**
 * This is {@link PullRequestListener}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface PullRequestListener {
    void onOpenPullRequest(PullRequest pr);
}
