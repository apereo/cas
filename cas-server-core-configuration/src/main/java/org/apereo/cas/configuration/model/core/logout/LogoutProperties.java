package org.apereo.cas.configuration.model.core.logout;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class LogoutProperties {

    private boolean followServiceRedirects;

    public boolean isFollowServiceRedirects() {
        return followServiceRedirects;
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }
}
