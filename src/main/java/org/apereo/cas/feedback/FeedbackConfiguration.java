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

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apereo.cas.GitHubProperties;
import org.apereo.cas.IssueListener;
import org.apereo.cas.github.GitHubOperations;

/**
 * Central configuration for the beans involved in managing issues that are waiting for
 * feedback.
 *
 * @author Andy Wilkinson
 */
@Configuration
@EnableConfigurationProperties(FeedbackProperties.class)
class FeedbackConfiguration {

	@Bean
	FeedbackIssueListener feedbackIssueListener(GitHubOperations gitHub,
			GitHubProperties githubProperties, FeedbackProperties feedbackProperties,
			List<IssueListener> issueListener) {
		return new FeedbackIssueListener(gitHub, feedbackProperties.getRequiredLabel(),
				githubProperties.getRepository().getCollaborators(),
				githubProperties.getCredentials().getUsername(),
				new StandardFeedbackListener(gitHub,
						feedbackProperties.getProvidedLabel(),
						feedbackProperties.getRequiredLabel(),
						feedbackProperties.getReminderLabel(),
						feedbackProperties.getReminderComment(),
						feedbackProperties.getCloseComment(), issueListener));
	}

}
