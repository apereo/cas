package org.apereo.cas.prs;

import org.apereo.cas.GitHubProperties;
import org.apereo.cas.PullRequestListener;
import org.apereo.cas.github.GitHubOperations;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link PullRequestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration
@EnableConfigurationProperties(PullRequestProperties.class)
class PullRequestConfiguration {
    @Bean
    PullRequestListener pullRequestListener(GitHubOperations gitHub,
                                            GitHubProperties githubProperties,
                                            PullRequestProperties pullRequestProperties) {
        return new StandardPullRequestListener(gitHub, githubProperties, pullRequestProperties);
    }

}
