package org.jasig.cas.authentication;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Handles both remember me services and username and password.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RememberMeUsernamePasswordCredential extends UsernamePasswordCredential implements RememberMeCredential {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -6710007659431302397L;

    private boolean rememberMe;

    @Override
    public final boolean isRememberMe() {
        return this.rememberMe;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(rememberMe)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RememberMeUsernamePasswordCredential other = (RememberMeUsernamePasswordCredential) obj;
        if (this.rememberMe != other.rememberMe) {
            return false;
        }
        return true;
    }

    @Override
    public final void setRememberMe(final boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
