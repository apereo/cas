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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring.issuebot.triage.github.Issue;

/**
 * A {@link TriageFilter} that delegates to one or more filters.
 *
 * @author Andy Wilkinson
 */
final class DelegatingTriageFilter implements TriageFilter {

	private static final Logger log = LoggerFactory
			.getLogger(DelegatingTriageFilter.class);

	private final List<TriageFilter> filters;

	DelegatingTriageFilter(List<TriageFilter> filters) {
		this.filters = filters;
	}

	@Override
	public boolean triaged(Issue issue) {
		for (TriageFilter filter : this.filters) {
			if (filter.triaged(issue)) {
				return true;
			}
		}
		log.info("{} is waiting for triage", issue.getUrl());
		return false;
	}

}
