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

package io.spring.issuebot.triage.filter;

import java.util.Arrays;

import io.spring.issuebot.triage.MonitoredRepository;
import io.spring.issuebot.triage.github.GitHubOperations;

/**
 * Standard implementation of {@code TriageFilters}.
 *
 * @author Andy Wilkinson
 */
public class StandardTriageFilters implements TriageFilters {

	private final GitHubOperations gitHub;

	/**
	 * Creates a new {@code StandardTriageFilters} that will use the given
	 * {@code gitHubOperations} to interact with GitHub.
	 *
	 * @param gitHubOperations the GitHubOperations
	 */
	public StandardTriageFilters(GitHubOperations gitHubOperations) {
		this.gitHub = gitHubOperations;
	}

	@Override
	public TriageFilter filterForRepository(MonitoredRepository repository) {
		return new DelegatingTriageFilter(Arrays.asList(
				new OpenedByCollaboratorTriageFilter(repository.getCollaborators()),
				new LabelledTriageFilter(), new MilestoneAppliedTriageFilter(),
				new CommentedByCollaboratorTriageFilter(repository.getCollaborators(),
						this.gitHub)));
	}

}
