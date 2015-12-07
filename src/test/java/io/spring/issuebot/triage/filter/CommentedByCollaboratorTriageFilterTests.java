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
import java.util.Collections;

import org.junit.Test;

import io.spring.issuebot.github.Comment;
import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.Issue;
import io.spring.issuebot.github.Page;
import io.spring.issuebot.github.User;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link CommentedByCollaboratorTriageFilter}.
 *
 * @author Andy Wilkinson
 *
 */
public class CommentedByCollaboratorTriageFilterTests {

	private final GitHubOperations gitHub = mock(GitHubOperations.class);

	private final TriageFilter filter = new CommentedByCollaboratorTriageFilter(
			Arrays.asList("Adam", "Brenda", "Charlie"), this.gitHub);

	private final Issue issue = new Issue(null, null, null, null, null);

	@Test
	@SuppressWarnings("unchecked")
	public void noComments() {
		Page<Comment> pageOne = mock(Page.class);
		given(pageOne.getContent()).willReturn(Collections.emptyList());
		given(this.gitHub.getComments(this.issue)).willReturn(pageOne);
		assertThat(this.filter.triaged(this.issue), is(false));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void noCommentsByCollaborators() {
		Page<Comment> pageOne = mock(Page.class);
		given(pageOne.getContent())
				.willReturn(Arrays.asList(new Comment(new User("Debbie"))));
		given(this.gitHub.getComments(this.issue)).willReturn(pageOne);
		assertThat(this.filter.triaged(this.issue), is(false));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void commentByCollaboratorOnFirstPage() {
		Page<Comment> pageOne = mock(Page.class);
		given(pageOne.getContent())
				.willReturn(Arrays.asList(new Comment(new User("Brenda"))));
		given(this.gitHub.getComments(this.issue)).willReturn(pageOne);
		assertThat(this.filter.triaged(this.issue), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void commentByCollaboratorOnLaterPage() {
		Page<Comment> pageOne = mock(Page.class);
		given(pageOne.getContent())
				.willReturn(Arrays.asList(new Comment(new User("Debbie"))));
		Page<Comment> pageTwo = mock(Page.class);
		given(pageTwo.getContent())
				.willReturn(Arrays.asList(new Comment(new User("Brenda"))));
		given(pageOne.next()).willReturn(pageTwo);
		given(this.gitHub.getComments(this.issue)).willReturn(pageOne);
		assertThat(this.filter.triaged(this.issue), is(true));
	}

}
