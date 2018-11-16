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
import org.apereo.cas.github.Issue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import org.apereo.cas.github.Page;

/**
 * Central class for monitoring the configured repository.
 *
 * @author Andy Wilkinson
 */
class RepositoryMonitor {

	private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

	private final GitHubOperations gitHub;

	private final MonitoredRepository repository;

	private final List<IssueListener> issueListeners;

	RepositoryMonitor(GitHubOperations gitHub, MonitoredRepository repository,
			List<IssueListener> issueListeners) {
		this.gitHub = gitHub;
		this.repository = repository;
		this.issueListeners = issueListeners;
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	void monitor() {
		log.info("Monitoring {}/{}", this.repository.getOrganization(),
				this.repository.getName());
		try {
			Page<Issue> page = this.gitHub.getIssues(this.repository.getOrganization(),
					this.repository.getName());
			while (page != null) {
				for (Issue issue : page.getContent()) {
					for (IssueListener issueListener : this.issueListeners) {
						try {
							issueListener.onOpenIssue(issue);
						}
						catch (Exception ex) {
							log.warn("Listener '{}' failed when handling issue '{}'",
									issueListener, issue, ex);
						}
					}
				}
				page = page.next();
			}
		}
		catch (Exception ex) {
			log.warn("A failure occurred during issue monitoring", ex);
		}
		log.info("Monitoring of {}/{} completed", this.repository.getOrganization(),
				this.repository.getName());
	}

}
