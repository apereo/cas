package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@ToString
public class DefaultRegisteredServiceExpirationPolicy implements RegisteredServiceExpirationPolicy {

    private static final long serialVersionUID = 5106652807554743500L;

    private boolean deleteWhenExpired;

    private boolean notifyWhenDeleted;

    private String expirationDate;

    public DefaultRegisteredServiceExpirationPolicy() {
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final boolean notifyWhenDeleted, final String expirationDate) {
        this.deleteWhenExpired = deleteWhenExpired;
        this.notifyWhenDeleted = notifyWhenDeleted;
        this.expirationDate = expirationDate;
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final String expirationDate) {
        this(deleteWhenExpired, false, expirationDate);
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDate expirationDate) {
        this(deleteWhenExpired, false, expirationDate.toString());
    }

    public DefaultRegisteredServiceExpirationPolicy(final boolean deleteWhenExpired, final LocalDateTime expirationDate) {
        this(deleteWhenExpired, false, expirationDate.toString());
    }

    public DefaultRegisteredServiceExpirationPolicy(final LocalDateTime expirationDate) {
        this(true, expirationDate);
    }

    public DefaultRegisteredServiceExpirationPolicy(final LocalDate expirationDate) {
        this(true, expirationDate.toString());
    }

    @Override
    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean isNotifyWhenDeleted() {
        return notifyWhenDeleted;
    }

    public void setNotifyWhenDeleted(final boolean notifyWhenDeleted) {
        this.notifyWhenDeleted = notifyWhenDeleted;
    }

    @Override
    public boolean isDeleteWhenExpired() {
        return deleteWhenExpired;
    }

    public void setDeleteWhenExpired(final boolean deleteWhenExpired) {
        this.deleteWhenExpired = deleteWhenExpired;
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
        final DefaultRegisteredServiceExpirationPolicy rhs = (DefaultRegisteredServiceExpirationPolicy) obj;
        return new EqualsBuilder().append(this.deleteWhenExpired, rhs.deleteWhenExpired).append(this.notifyWhenDeleted, rhs.notifyWhenDeleted).append(this.expirationDate, rhs.expirationDate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 137).append(deleteWhenExpired).append(notifyWhenDeleted).append(expirationDate).toHashCode();
    }
}
