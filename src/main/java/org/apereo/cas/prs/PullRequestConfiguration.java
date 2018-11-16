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
class PullRequestConfiguration {
    @Bean
    public PullRequestListener pullRequestListener(final GitHubOperations gitHub,
                                            final GitHubProperties githubProperties) {
        return new ApereoCasPullRequestListener(gitHub, githubProperties);
    }

}
