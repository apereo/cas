package org.apereo.cas.prs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link PullRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "casbot.prs")
public class PullRequestProperties {
}
