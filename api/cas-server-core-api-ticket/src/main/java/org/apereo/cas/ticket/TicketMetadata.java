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

    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
    }

    public Class<? extends Ticket> getImplementationClass() {
        return implementationClass;
    }

    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("implementationClass", implementationClass)
                .append("prefix", prefix)
                .toString();
    }
}
