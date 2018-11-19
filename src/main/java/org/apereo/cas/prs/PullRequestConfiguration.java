package org.apereo.cas.prs;

import org.apereo.cas.MonitoredRepository;
import org.apereo.cas.PullRequestListener;

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
    public PullRequestListener pullRequestListener(final MonitoredRepository repository) {
        return new ApereoCasPullRequestListener(repository);
    }

}
