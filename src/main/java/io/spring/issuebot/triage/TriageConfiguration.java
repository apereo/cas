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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.spring.issuebot.triage.filter.StandardTriageFilters;
import io.spring.issuebot.triage.github.GitHubTemplate;
import io.spring.issuebot.triage.github.RegexLinkParser;

/**
 * Central configuration for the beans involved in identifying issues that require triage.
 *
 * @author Andy Wilkinson
 */
@Configuration
@EnableScheduling
class TriageConfiguration {

	@Bean
	TriageProperties triageProperties() {
		return new TriageProperties();
	}

	@Bean
	LabelApplyingTriageListener triageListener() {
		return new LabelApplyingTriageListener(gitHubTemplate());
	}

	@Bean
	RepositoryMonitor repositoryMonitor() {
		return new RepositoryMonitor(gitHubTemplate(), triageFilters(), triageListener(),
				triageProperties().getRepositories());
	}

	@Bean
	GitHubTemplate gitHubTemplate() {
		TriageProperties triageProperties = triageProperties();
		return new GitHubTemplate(triageProperties.getUsername(),
				triageProperties.getPassword(), linkParser());
	}

	@Bean
	StandardTriageFilters triageFilters() {
		return new StandardTriageFilters(gitHubTemplate());
	}

	@Bean
	RegexLinkParser linkParser() {
		return new RegexLinkParser();
	}

}
