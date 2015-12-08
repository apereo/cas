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

import io.spring.issuebot.IssueListener;
import io.spring.issuebot.github.Issue;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link TriageIssueListener}.
 *
 * @author Andy Wilkinson
 */
public class TriageIssueListenerTests {

	private final TriageFilter triageFilterOne = mock(TriageFilter.class);

	private final TriageFilter triageFilterTwo = mock(TriageFilter.class);

	private final TriageListener listener = mock(TriageListener.class);

	private final IssueListener issueListener = new TriageIssueListener(
			Arrays.asList(this.triageFilterOne, this.triageFilterTwo), this.listener);

	@Test
	public void listenerIsCalledWhenIssueRequiresTriage() {
		Issue issue = new Issue(null, null, null, null, null, null, null, null);
		given(this.triageFilterOne.triaged(issue)).willReturn(false);
		given(this.triageFilterTwo.triaged(issue)).willReturn(false);
		this.issueListener.onOpenIssue(issue);
		verify(this.triageFilterOne).triaged(issue);
		verify(this.triageFilterTwo).triaged(issue);
		verify(this.listener).requiresTriage(issue);
	}

	@Test
	public void listenerIsNotCalledWhenIssueHasAlreadyBeenTriaged() {
		Issue issue = new Issue(null, null, null, null, null, null, null, null);
		given(this.triageFilterOne.triaged(issue)).willReturn(true);
		this.issueListener.onOpenIssue(issue);
		verify(this.triageFilterOne).triaged(issue);
		verifyNoMoreInteractions(this.triageFilterTwo, this.listener);
	}

}
