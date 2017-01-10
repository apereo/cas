package org.apereo.cas;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;

/**
 * This is {@link GoogleAuthenticatorToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GoogleAuthenticatorToken {
    private final Integer token;
    private final String userId;
    private final LocalDateTime issuedDateTime = LocalDateTime.now();

    public GoogleAuthenticatorToken(final Integer token, final String userId) {
        this.token = token;
        this.userId = userId;
    }

    public Integer getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getIssuedDateTime() {
        return issuedDateTime;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", token)
                .append("userId", userId)
                .append("issuedDateTime", issuedDateTime)
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
        final GoogleAuthenticatorToken rhs = (GoogleAuthenticatorToken) obj;
        return new EqualsBuilder()
                .append(this.token, rhs.token)
                .append(this.userId, rhs.userId)
                .append(this.issuedDateTime, rhs.issuedDateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(token)
                .append(userId)
                .append(issuedDateTime)
                .toHashCode();
    }
}
