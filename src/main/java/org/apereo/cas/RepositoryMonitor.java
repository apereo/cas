/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas;

import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Page;
import org.apereo.cas.github.PullRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Central class for monitoring the configured repository.
 *
 * @author Andy Wilkinson
 */
@Slf4j
class RepositoryMonitor {

    private final GitHubOperations gitHub;

    private final MonitoredRepository repository;

    private final List<PullRequestListener> pullRequestListeners;

    RepositoryMonitor(final GitHubOperations gitHub, final MonitoredRepository repository,
                      final List<PullRequestListener> pullRequestListeners) {
        this.gitHub = gitHub;
        this.repository = repository;
        this.pullRequestListeners = pullRequestListeners;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    void monitor() {
        log.info("Monitoring {}/{}", this.repository.getOrganization(),this.repository.getName());
        try {
            Page<PullRequest> page = this.gitHub.getPullRequests(this.repository.getOrganization(), this.repository.getName());
            while (page != null) {
                for (final PullRequest pr : page.getContent()) {
                    for (final PullRequestListener listener : this.pullRequestListeners) {
                        try {
                            if (pr.isOpen()) {
                                listener.onOpenPullRequest(pr);
                            }
                        } catch (final Exception ex) {
                            log.warn("Listener '{}' failed when handling pr '{}'", listener, pr, ex);
                        }
                    }
                }
                page = page.next();
            }
        } catch (final Exception ex) {
            log.warn("A failure occurred during monitoring", ex);
        }
        log.info("Monitoring of {}/{} completed", this.repository.getOrganization(), this.repository.getName());
    }

}
