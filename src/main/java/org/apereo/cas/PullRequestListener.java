package org.apereo.cas;

import org.apereo.cas.github.PullRequest;

public interface PullRequestListener {
    void onOpenPullRequest(PullRequest pr) throws Exception;

    void onOpenPullRequest(String number) throws Exception;

    MonitoredRepository getRepository();
}
