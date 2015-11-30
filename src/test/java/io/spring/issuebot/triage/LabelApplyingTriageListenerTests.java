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

import org.junit.Test;

import io.spring.issuebot.triage.github.GitHubOperations;
import io.spring.issuebot.triage.github.Issue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link LabelApplyingTriageListener}.
 *
 * @author Andy Wilkinson
 */
public class LabelApplyingTriageListenerTests {

	private GitHubOperations gitHub = mock(GitHubOperations.class);

	private final MonitoredRepository repository = new MonitoredRepository();

	private final LabelApplyingTriageListener listener = new LabelApplyingTriageListener(
			this.gitHub);

	@Test
	public void requiresTriage() {
		Issue issue = new Issue(null, null, null, null, null);
		this.repository.setLabel("test");
		this.listener.requiresTriage(issue, this.repository);
		verify(this.gitHub).addLabel(issue, "test");
	}

}
