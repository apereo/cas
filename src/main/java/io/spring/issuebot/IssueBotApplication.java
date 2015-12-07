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

package io.spring.issuebot;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.spring.issuebot.github.GitHubOperations;
import io.spring.issuebot.github.GitHubTemplate;
import io.spring.issuebot.github.RegexLinkParser;

/**
 * Main class for launching Issue Bot.
 *
 * @author Andy Wilkinson
 */
@SpringBootApplication
@EnableConfigurationProperties(GitHubProperties.class)
public class IssueBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueBotApplication.class, args);
	}

	@Bean
	GitHubTemplate gitHubTemplate(GitHubProperties gitHubProperties) {
		return new GitHubTemplate(gitHubProperties.getCredentials().getUsername(),
				gitHubProperties.getCredentials().getPassword(), new RegexLinkParser());
	}

	@Bean
	RepositoryMonitor repositoryMonitor(GitHubOperations gitHub,
			GitHubProperties gitHubProperties, List<IssueListener> issueListeners) {
		return new RepositoryMonitor(gitHub,
				new MonitoredRepository(
						gitHubProperties.getRepository().getOrganization(),
						gitHubProperties.getRepository().getName()),
				issueListeners);
	}

}
