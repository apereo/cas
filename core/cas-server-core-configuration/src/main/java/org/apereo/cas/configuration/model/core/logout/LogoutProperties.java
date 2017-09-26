package org.apereo.cas.configuration.model.core.logout;

import org.apereo.cas.configuration.support.RequiredModule;

import java.io.Serializable;

/**
 * This is {@link LogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredModule(name = "cas-server-core-logout", automated = true)
public class LogoutProperties implements Serializable {
    private static final long serialVersionUID = 7466171260665661949L;
    /**
     * The target destination to which CAS should redirect after logout
     * is indicated and extracted by a parameter name of your choosing here. If none specified,
     * the default will be used as {@code service}.
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
