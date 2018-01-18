package org.apereo.cas.adaptors.duo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DuoUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
@Setter
public class DuoUserAccount {

    private DuoUserAccountAuthStatus status = DuoUserAccountAuthStatus.AUTH;

    private String enrollPortalUrl;

    private String username;

    private String message;

    public DuoUserAccount(final String username) {
        this.username = username;
    }

    public DuoUserAccountAuthStatus getStatus() {
        return status;
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
        return new EqualsBuilder().append(this.status, rhs.status).append(this.enrollPortalUrl, rhs.enrollPortalUrl).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(enrollPortalUrl).toHashCode();
    }
}
