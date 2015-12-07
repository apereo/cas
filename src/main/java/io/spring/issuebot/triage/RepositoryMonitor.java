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

package io.spring.issuebot.triage;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;
import io.spring.issuebot.triage.filter.TriageFilter;
import io.spring.issuebot.triage.filter.TriageFilters;

/**
 * Central class for monitoring the configured repositories and labeling issues as waiting
 * for triage.
 *
 * @author Andy Wilkinson
 */
class RepositoryMonitor {

	private static final Logger log = LoggerFactory.getLogger(RepositoryMonitor.class);

	private final List<MonitoredRepository> repositoryConfigurations;

	private final TriageFilters filters;

	private final GitHubOperations gitHub;

	private final TriageListener listener;

	RepositoryMonitor(GitHubOperations gitHub, TriageFilters filters,
			TriageListener listener, List<MonitoredRepository> repositoryConfigurations) {
		this.gitHub = gitHub;
		this.filters = filters;
		this.listener = listener;
		this.repositoryConfigurations = repositoryConfigurations;
	}

	@Scheduled(fixedRate = 5 * 60 * 1000)
	void monitor() {
		for (MonitoredRepository configuration : this.repositoryConfigurations) {
			monitor(configuration);
		}
	}

	private void monitor(MonitoredRepository repository) {
		log.info("Monitoring {}/{}", repository.getOrganization(), repository.getName());
		TriageFilter filter = this.filters.filterForRepository(repository);
		Page<Issue> page = this.gitHub.getIssues(repository.getOrganization(),
				repository.getName());
		while (page != null) {
			for (Issue issue : page.getContent()) {
				if (!filter.triaged(issue)) {
					this.listener.requiresTriage(issue, repository);
				}
			}
			page = page.next();
		}
		log.info("Monitoring of {}/{} completed", repository.getOrganization(),
				repository.getName());
	}

}
