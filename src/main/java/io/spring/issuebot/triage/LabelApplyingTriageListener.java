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

import io.spring.issuebot.triage.github.GitHubOperations;
import io.spring.issuebot.triage.github.Issue;

/**
 * A {@link TriageListener} that applies a label to any issues that require triage.
 *
 * @author Andy Wilkinson
 */
public class LabelApplyingTriageListener implements TriageListener {

	private final GitHubOperations gitHub;

	/**
	 * Creates a new {@code LabelApplyingTriageListener} that will use the given
	 * {@code gitHubOperations} to apply a label to any issues that require triage.
	 *
	 * @param gitHubOperations the GitHubOperations
	 */
	public LabelApplyingTriageListener(GitHubOperations gitHubOperations) {
		this.gitHub = gitHubOperations;
	}

	@Override
	public void requiresTriage(Issue issue, MonitoredRepository repository) {
		this.gitHub.addLabel(issue, repository.getLabel());
	}

}
