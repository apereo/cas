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

import org.apereo.cas.prs.PullRequestProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConfigurationProperties} for connecting to GitHub.
 *
 * @author Andy Wilkinson
 */
@ConfigurationProperties(prefix = "casbot.github", ignoreUnknownFields = false)
@Getter
@Setter
@Validated
public class GitHubProperties {

    @NestedConfigurationProperty
    @Valid
    private Repository repository = new Repository();
    
    @NestedConfigurationProperty
    @Valid
    private Repository stagingRepository = new Repository();

    @NestedConfigurationProperty
    private PullRequestProperties prs = new PullRequestProperties();

    private long maximumChangedFiles = 40;

    private long staleWorkflowRunInDays = 5;

    private long completedSuccessfulWorkflowRunInDays = 1;

    private long completedFailedWorkflowRunInDays = 2;

    /**
     * Configuration for a GitHub repository.
     */
    @Getter
    @Setter
    @Validated
    public static class Repository {

        /**
         * The name of the organization that owns the repository.
         */
        private String organization;

        /**
         * The name of the repository.
         */
        private String name;

        private String url;

        private List<String> committers = new ArrayList<>();
        private List<String> admins = new ArrayList<>();

        @NestedConfigurationProperty
        @Valid
        private Credentials credentials = new Credentials();
        
        public String getFullName() {
            return organization + '/' + name;
        }
    }

    /**
     * Configuration for the credentials used to authenticate with GitHub.
     */
    @Getter
    @Setter
    @Validated
    public static class Credentials {
        @NotBlank
        private String token;
    }

}
