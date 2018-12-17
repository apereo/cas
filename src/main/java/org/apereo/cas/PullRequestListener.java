package org.apereo.cas;

import org.apereo.cas.github.PullRequest;

/**
 * This is {@link PullRequestListener}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface PullRequestListener {
    default void onOpenPullRequest(final PullRequest pr) {

    }

    default void onPullRequestClosure(final PullRequest pr) {
    }
}
