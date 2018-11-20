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

package org.apereo.cas;

import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.GitHubTemplate;
import org.apereo.cas.github.RegexLinkParser;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(GitHubProperties.class)
public class CasBotApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CasBotApplication.class, args);
    }

    @Bean
    public GitHubTemplate gitHubTemplate(final GitHubProperties gitHubProperties) {
        if (gitHubProperties.getCredentials().getPassword() == null) {
            throw new BeanCreationException("No credential password is specified");
        }
        if (gitHubProperties.getCredentials().getUsername() == null) {
            throw new BeanCreationException("No credential username is specified");
        }
        return new GitHubTemplate(gitHubProperties.getCredentials().getUsername(),
            gitHubProperties.getCredentials().getPassword(), new RegexLinkParser());
    }

    @Bean
    public MonitoredRepository monitoredRepository(final GitHubOperations gitHub,
                                                   final GitHubProperties gitHubProperties) {
        return new MonitoredRepository(gitHub, gitHubProperties);
    }

    @Bean
    public RepositoryMonitor repositoryMonitor(final GitHubOperations gitHub,
                                               final MonitoredRepository repository,
                                               final List<PullRequestListener> pullRequestListeners) {
        return new RepositoryMonitor(gitHub, repository, pullRequestListeners);
    }

    @RestController
    public static class HomeController {

        @Autowired
        private MonitoredRepository repository;

        @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String, String> home() {
             var map = new LinkedHashMap<String, String>();
             map.put("name", repository.getOrganization() + "/" + repository.getName());
             map.put("repository", repository.getGitHubProperties().getRepository().getUrl());
             map.put("version", repository.getCurrentVersionInMaster().toString());
             return map;
        }
    }
}
