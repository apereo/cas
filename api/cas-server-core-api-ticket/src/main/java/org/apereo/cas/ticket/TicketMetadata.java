package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link TicketMetadata}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TicketMetadata {
    private Class<? extends Ticket> implementationClass;
    private String prefix;
    private boolean cascading;
    private Map<String, Object> properties = new HashMap<>();

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix) {
        this(implementationClass, prefix, false);
    }

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix, final boolean cascading) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.cascading = cascading;
    }

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix,
                          final Map<String, Object> properties) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.properties = properties;
    }

    public String getPropertyAsString(final String key) {
        return getProperty(key, String.class);
    }

    public String getPropertyAsNumber(final String key) {
        return getProperty(key, Long.class);
    }

    public <T> T getProperty(final String key, final Class<?> clazz) {
        return (T) clazz.getClass().cast(properties.get(key));
    }

    public void setProperty(final String key, final Object value) {
        properties.put(key, value);
    }

    public Class<? extends Ticket> getImplementationClass() {
        return implementationClass;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isCascading() {
        return cascading;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("implementationClass", implementationClass)
                .append("prefix", prefix)
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
        final TicketMetadata rhs = (TicketMetadata) obj;
        return new EqualsBuilder()
                .append(this.implementationClass, rhs.implementationClass)
                .append(this.prefix, rhs.prefix)
                .append(this.cascading, rhs.cascading)
                .append(this.properties, rhs.properties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(implementationClass)
                .append(prefix)
                .append(cascading)
                .append(properties)
                .toHashCode();
    }
}
