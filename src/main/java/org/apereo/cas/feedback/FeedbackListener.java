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

package org.apereo.cas.feedback;

import java.time.OffsetDateTime;

import org.apereo.cas.github.Issue;

/**
 * A {@code FeedbackListener} is notified when feedback has been provided or is still
 * required for an {@link Issue}.
 *
 * @author Andy Wilkinson
 */
public interface FeedbackListener {

	/**
	 * Notification that feedback has been provided for the given {@code issue}.
	 *
	 * @param issue the issue
	 */
	void feedbackProvided(Issue issue);

	/**
	 * Notification that feedback is still required for the given {@code issue} having
	 * been requested at the given {@code requestTime}.
	 *
	 * @param issue the issue
	 * @param requestTime the time when feedback was requested
	 */
	void feedbackRequired(Issue issue, OffsetDateTime requestTime);

}
