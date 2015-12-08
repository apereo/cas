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

package io.spring.issuebot;

import java.util.Arrays;

import org.junit.Test;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link RepositoryMonitor}.
 *
 * @author Andy Wilkinson
 */
public class RepositoryMonitorTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final IssueListener issueListenerOne = mock(IssueListener.class);

	private final IssueListener issueListenerTwo = mock(IssueListener.class);

	private final RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
			new MonitoredRepository("test", "test"),
			Arrays.asList(this.issueListenerOne, this.issueListenerTwo));

	@Test
	public void repositoryWithNoIssues() {
		given(this.gitHub.getIssues("test", "test")).willReturn(null);
		this.repositoryMonitor.monitor();
		verifyNoMoreInteractions(this.issueListenerOne, this.issueListenerTwo);
	}

	@Test
	public void repositoryWithOpenIssues() {
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issueOne = new Issue(null, null, null, null, null, null, null, null);
		Issue issueTwo = new Issue(null, null, null, null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issueOne, issueTwo));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		this.repositoryMonitor.monitor();
		verify(this.issueListenerOne).onOpenIssue(issueOne);
		verify(this.issueListenerOne).onOpenIssue(issueTwo);
		verify(this.issueListenerTwo).onOpenIssue(issueOne);
		verify(this.issueListenerTwo).onOpenIssue(issueTwo);
	}

}
