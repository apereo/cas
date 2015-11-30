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

import io.spring.issuebot.triage.MonitoredRepository;

/**
 * {@code TriageFilters} is used to retrieve a {@code TriageFilter} for a particular
 * {@link MonitoredRepository repository}.
 *
 * @author Andy Wilkinson
 */
public interface TriageFilters {

	/**
	 * Returns the {@code TriageFilter} that should be used to process issues for the
	 * given {@code repository}.
	 *
	 * @param repository the repository
	 * @return the filter for the repository
	 */
	TriageFilter filterForRepository(MonitoredRepository repository);

}
