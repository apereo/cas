package org.apereo.cas.configuration.model.core.logout;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.logout", ignoreUnknownFields = false)
public class LogoutProperties {

    private boolean followServiceRedirects;

    public boolean isFollowServiceRedirects() {
        return followServiceRedirects;
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }
}
