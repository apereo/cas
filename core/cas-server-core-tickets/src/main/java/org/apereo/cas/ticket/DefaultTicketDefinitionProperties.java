package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is {@link DefaultTicketDefinitionProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketDefinitionProperties implements TicketDefinitionProperties {

    /**
     * Whether ticket operations require cascading down in the storage.
     */
    private boolean cascadeTicket;
    /**
     * Storage/cache name that holds this ticket.
     */
    private String cacheName;
    /**
     * Timeout for this ticket.
     */
    private long cacheTimeout;
    /**
     * Password for this ticket storage, if any.
     */
    private String storagePassword;

    @Override
    public long getStorageTimeout() {
        return cacheTimeout;
    }

    @Override
    public void setStorageTimeout(final long timeout) {
        this.cacheTimeout = timeout;
    }

    @Override
    public boolean isCascade() {
        return cascadeTicket;
    }

    @Override
    public void setCascade(final boolean cascadeTicket) {
        this.cascadeTicket = cascadeTicket;
    }

    @Override
    public String getStorageName() {
        return cacheName;
    }

    @Override
    public void setStorageName(final String storageName) {
        this.cacheName = storageName;
    }
    
    @Override
    public String getStoragePassword() {
        return storagePassword;
    }

    @Override
    public void setStoragePassword(final String storagePassword) {
        this.storagePassword = storagePassword;
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
        final DefaultTicketDefinitionProperties rhs = (DefaultTicketDefinitionProperties) obj;
        return new EqualsBuilder()
                .append(this.cascadeTicket, rhs.cascadeTicket)
                .append(this.cacheName, rhs.cacheName)
                .append(this.cacheTimeout, rhs.cacheTimeout)
                .append(this.storagePassword, rhs.storagePassword)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(cascadeTicket)
                .append(cacheName)
                .append(cacheTimeout)
                .append(storagePassword)
                .toHashCode();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("cascadeTicket", cascadeTicket)
                .append("cacheName", cacheName)
                .append("cacheTimeout", cacheTimeout)
                .toString();
    }
}
