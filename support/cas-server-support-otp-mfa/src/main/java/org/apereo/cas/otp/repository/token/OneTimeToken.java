package org.apereo.cas.otp.repository.token;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This is {@link OneTimeToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class OneTimeToken implements Serializable, Comparable<OneTimeToken> {
    private static final long serialVersionUID = -1329938047176583075L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private Integer token;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime issuedDateTime = LocalDateTime.now();

    public OneTimeToken() {
        setId(java.lang.System.currentTimeMillis());
    }

    public OneTimeToken(final Integer token, final String userId) {
        this();
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

    public void setId(final long id) {
        this.id = id;
    }

    public void setToken(final Integer token) {
        this.token = token;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setIssuedDateTime(final LocalDateTime issuedDateTime) {
        this.issuedDateTime = issuedDateTime;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", token)
                .append("userId", userId)
                .append("id", id)
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
        final OneTimeToken rhs = (OneTimeToken) obj;
        return new EqualsBuilder()
                .append(this.token, rhs.token)
                .append(this.userId, rhs.userId)
                .append(this.id, rhs.id)
                .append(this.issuedDateTime, rhs.issuedDateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(token)
                .append(userId)
                .append(issuedDateTime)
                .append(id)
                .toHashCode();
    }

    @Override
    public int compareTo(final OneTimeToken o) {
        return new CompareToBuilder()
                .append(token, o.getToken())
                .append(userId, o.getUserId())
                .append(issuedDateTime, o.getIssuedDateTime())
                .append(id, o.id)
                .build();
    }
}
