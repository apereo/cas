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

package org.apereo.cas.triage;

import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Issue;

/**
 * A {@link TriageListener} that applies a label to any issues that require triage.
 *
 * @author Andy Wilkinson
 */
final class LabelApplyingTriageListener implements TriageListener {

	private final GitHubOperations gitHub;

	private final String label;

	/**
	 * Creates a new {@code LabelApplyingTriageListener} that will use the given
	 * {@code gitHubOperations} to apply the given {@code label} to any issues that
	 * require triage.
	 *
	 * @param gitHubOperations the GitHubOperations
	 * @param label the label
	 */
	LabelApplyingTriageListener(GitHubOperations gitHubOperations, String label) {
		this.gitHub = gitHubOperations;
		this.label = label;
	}

	@Override
	public void requiresTriage(Issue issue) {
		this.gitHub.addLabel(issue, this.label);
	}

	@Override
	public void doesNotRequireTriage(Issue issue) {
		this.gitHub.removeLabel(issue, this.label);
	}

}
