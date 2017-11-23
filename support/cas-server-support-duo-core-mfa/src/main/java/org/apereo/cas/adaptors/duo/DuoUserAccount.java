package org.apereo.cas.adaptors.duo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is {@link DuoUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DuoUserAccount {
    private DuoUserAccountAuthStatus status = DuoUserAccountAuthStatus.AUTH;
    private String enrollPortalUrl;
    private String username;
    private String message;

    public DuoUserAccount(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getEnrollPortalUrl() {
        return enrollPortalUrl;
    }

    public void setEnrollPortalUrl(final String enrollPortalUrl) {
        this.enrollPortalUrl = enrollPortalUrl;
    }

    public DuoUserAccountAuthStatus getStatus() {
        return status;
    }

    public void setStatus(final DuoUserAccountAuthStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("status", status)
                .append("enrollPortalUrl", enrollPortalUrl)
                .toString();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final DuoUserAccount rhs = (DuoUserAccount) obj;
        return new EqualsBuilder()
                .append(this.status, rhs.status)
                .append(this.enrollPortalUrl, rhs.enrollPortalUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(status)
                .append(enrollPortalUrl)
                .toHashCode();
    }
}
