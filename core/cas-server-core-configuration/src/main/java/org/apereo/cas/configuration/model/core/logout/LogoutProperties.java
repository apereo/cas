package org.apereo.cas.configuration.model.core.logout;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LogoutProperties {
    /**
     * The target destination to which CAS should redirect after logout
     * is indicated and extracted by a parameter name of your choosing here. If none specified,
     * the default will be used as <code>service</code>.
     */
    private String redirectParameter;
    /**
     * Whether CAS should be allowed to redirect to an alternative location after logout.
     */
    private boolean followServiceRedirects;

    /**
     * Before logout, allow the option to confirm on the web interface.
     */
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
