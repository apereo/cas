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

package io.spring.issuebot.feedback;

import java.time.OffsetDateTime;
import java.util.Arrays;

import org.junit.Test;

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.github.Comment;
import io.spring.issuebot.github.Event;
import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Label;
import io.spring.issuebot.github.PullRequest;
import io.spring.issuebot.github.StandardPage;
import io.spring.issuebot.github.User;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link FeedbackIssueListener}.
 *
 * @author Andy Wilkinson
 */
public class FeedbackIssueListenerTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final FeedbackListener feedbackListener = mock(FeedbackListener.class);

	private final IssueListener listener = new FeedbackIssueListener(this.gitHub,
			"required", Arrays.asList("Amy", "Brian"), this.feedbackListener);

	@Test
	public void pullRequestsAreIgnored() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("required")), null, new PullRequest("url"));
		this.listener.onOpenIssue(issue);
		verifyNoMoreInteractions(this.gitHub, this.feedbackListener);
	}

	@Test
	public void issuesWithFeedbackRequiredLabelAreIgnored() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("something-else")), null, null);
		this.listener.onOpenIssue(issue);
		verifyNoMoreInteractions(this.gitHub, this.feedbackListener);
	}

	@Test
	public void feedbackRequiredForLabeledIssueWithEvent() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now();
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Event("labeled", requestTime, new Label("required"))),
				() -> null));
		this.listener.onOpenIssue(issue);
		verify(this.feedbackListener).feedbackRequired(issue, requestTime);
	}

	@Test
	public void feedbackProvidedAfterCommentFromNonCollaborator() {
		Issue issue = new Issue("issue_url", null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now().minusDays(1);
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Event("labeled", requestTime, new Label("required"))),
				() -> null));
		given(this.gitHub.getComments(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Comment(new User("Charlie"), OffsetDateTime.now())),
				() -> null));
		this.listener.onOpenIssue(issue);
		verify(this.feedbackListener).feedbackProvided(issue);
	}

	@Test
	public void feedbackRequiredAfterCommentFromNonCollaboratorBeforeRequest() {
		Issue issue = new Issue("issue_url", null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now().minusDays(1);
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Event("labeled", requestTime, new Label("required"))),
				() -> null));
		given(this.gitHub.getComments(issue)).willReturn(new StandardPage<>(Arrays.asList(
				new Comment(new User("Charlie"), OffsetDateTime.now().minusDays(2))),
				() -> null));
		this.listener.onOpenIssue(issue);
		verify(this.feedbackListener).feedbackRequired(issue, requestTime);
	}

	@Test
	public void feedbackRequiredAfterCommentFromCollaborator() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now().minusDays(1);
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Event("labeled", requestTime, new Label("required"))),
				() -> null));
		given(this.gitHub.getComments(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Comment(new User("Amy"), OffsetDateTime.now())),
				() -> null));
		this.listener.onOpenIssue(issue);
		verify(this.feedbackListener).feedbackRequired(issue, requestTime);
	}

	@Test
	public void issueWithNoMatchingLabeledEventIsIgnored() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now();
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(
						new Event("labeled", requestTime, new Label("something-else"))),
				() -> null));
		this.listener.onOpenIssue(issue);
		verifyNoMoreInteractions(this.feedbackListener);
	}

	@Test
	public void eventsWithWrongTypeAreIgnored() {
		Issue issue = new Issue(null, null, null, null, null,
				Arrays.asList(new Label("required")), null, null);
		OffsetDateTime requestTime = OffsetDateTime.now();
		given(this.gitHub.getEvents(issue)).willReturn(new StandardPage<>(
				Arrays.asList(new Event("milestoned", requestTime, null)), () -> null));
		this.listener.onOpenIssue(issue);
		verifyNoMoreInteractions(this.feedbackListener);
	}

}
