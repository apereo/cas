package org.apereo.cas.configuration.model.core.logout;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LogoutProperties {
    private String redirectParameter;
    private boolean followServiceRedirects;
    private boolean confirmLogout;

    public boolean isConfirmLogout() {
        return confirmLogout;
    }

    public void setConfirmLogout(final boolean confirmLogout) {
        this.confirmLogout = confirmLogout;
    }

    public String getRedirectParameter() {
        return redirectParameter;
    }

    public void setRedirectParameter(final String redirectParameter) {
        this.redirectParameter = redirectParameter;
    }

    public boolean isFollowServiceRedirects() {
        return followServiceRedirects;
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }
}
