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

import java.util.Arrays;

import org.junit.Test;

import io.spring.issuebot.triage.filter.TriageFilter;
import io.spring.issuebot.triage.filter.TriageFilters;
import io.spring.issuebot.triage.github.GitHubOperations;
import io.spring.issuebot.triage.github.Issue;
import io.spring.issuebot.triage.github.Page;

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

	private final TriageFilters triageFilters = mock(TriageFilters.class);

	private final TriageFilter triageFilter = mock(TriageFilter.class);

	private final TriageListener listener = mock(TriageListener.class);

	@Test
	public void repositoryWithNoIssues() {
		MonitoredRepository repository = new MonitoredRepository();
		repository.setOrganization("test");
		repository.setName("test");
		RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
				this.triageFilters, this.listener, Arrays.asList(repository));
		given(this.triageFilters.filterForRepository(repository))
				.willReturn(this.triageFilter);
		given(this.gitHub.getIssues("test", "test")).willReturn(null);

		repositoryMonitor.monitor();

		verifyNoMoreInteractions(this.listener);
	}

	@Test
	public void repositoryWithIssueRequiringTriage() {
		MonitoredRepository repository = new MonitoredRepository();
		repository.setOrganization("test");
		repository.setName("test");
		RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
				this.triageFilters, this.listener, Arrays.asList(repository));
		given(this.triageFilters.filterForRepository(repository))
				.willReturn(this.triageFilter);
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issue));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		given(this.triageFilter.triaged(issue)).willReturn(false);

		repositoryMonitor.monitor();

		verify(this.listener).requiresTriage(issue, repository);
	}

	@Test
	public void repositoryWithIssueThatHasAlreadyBeenTriaged() {
		MonitoredRepository repository = new MonitoredRepository();
		repository.setOrganization("test");
		repository.setName("test");
		RepositoryMonitor repositoryMonitor = new RepositoryMonitor(this.gitHub,
				this.triageFilters, this.listener, Arrays.asList(repository));
		given(this.triageFilters.filterForRepository(repository))
				.willReturn(this.triageFilter);
		@SuppressWarnings("unchecked")
		Page<Issue> page = mock(Page.class);
		Issue issue = new Issue(null, null, null, null, null);
		given(page.getContent()).willReturn(Arrays.asList(issue));
		given(this.gitHub.getIssues("test", "test")).willReturn(page);
		given(this.triageFilter.triaged(issue)).willReturn(true);

		repositoryMonitor.monitor();

		verifyNoMoreInteractions(this.listener);
	}

}
