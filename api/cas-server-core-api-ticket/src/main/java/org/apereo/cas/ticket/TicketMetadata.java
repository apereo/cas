package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix) {
        this(implementationClass, prefix, false);
    }

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix, final boolean cascading) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.cascading = cascading;
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
}
