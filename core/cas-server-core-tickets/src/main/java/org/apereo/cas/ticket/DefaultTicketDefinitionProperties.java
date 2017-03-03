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

    private boolean cascadeTicket;
    private String cacheName;
    private long cacheTimeout;

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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(cascadeTicket)
                .append(cacheName)
                .append(cacheTimeout)
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
