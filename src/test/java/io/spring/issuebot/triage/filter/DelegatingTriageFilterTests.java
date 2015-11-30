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

import org.junit.Test;

import io.spring.issuebot.triage.github.Issue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link DelegatingTriageFilter}.
 *
 * @author Andy Wilkinson
 */
public class DelegatingTriageFilterTests {

	private TriageFilter delegate1 = mock(TriageFilter.class);

	private TriageFilter delegate2 = mock(TriageFilter.class);

	private TriageFilter delegate3 = mock(TriageFilter.class);

	private TriageFilter filter = new DelegatingTriageFilter(
			Arrays.asList(this.delegate1, this.delegate2, this.delegate3));

	@Test
	public void notTriagedWhenAllDelegatesReturnFalse() {
		Issue issue = new Issue(null, null, null, null, null);
		assertThat(this.filter.triaged(issue), is(false));
		verify(this.delegate1).triaged(issue);
		verify(this.delegate2).triaged(issue);
		verify(this.delegate3).triaged(issue);
	}

	@Test
	public void triagedAsSoonAsADelegateReturnsTrue() {
		Issue issue = new Issue(null, null, null, null, null);
		given(this.delegate2.triaged(issue)).willReturn(true);
		assertThat(this.filter.triaged(issue), is(true));
		verify(this.delegate1).triaged(issue);
		verify(this.delegate2).triaged(issue);
		verifyNoMoreInteractions(this.delegate3);

	}
}
