package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.core.Ordered;

/**
 * This is {@link DefaultTicketDefinition}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketDefinition implements TicketDefinition {
    private final Class<? extends Ticket> implementationClass;
    private final String prefix;
    private final TicketDefinitionProperties properties = new DefaultTicketDefinitionProperties();
    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Instantiates a new Ticket definition.
     *
     * @param implementationClass the implementation class
     * @param prefix              the prefix
     * @param order               the order
     */
    public DefaultTicketDefinition(final Class<? extends Ticket> implementationClass, final String prefix,
                                   final int order) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.order = order;
    }

    public DefaultTicketDefinition(final Class<? extends Ticket> implementationClass, final String prefix) {
        this(implementationClass, prefix, Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public Class<? extends Ticket> getImplementationClass() {
        return implementationClass;
    }

    @Override
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

    @Override
    public TicketDefinitionProperties getProperties() {
        return properties;
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
        final TicketDefinition rhs = (TicketDefinition) obj;
        return new EqualsBuilder()
                .append(this.implementationClass, rhs.getImplementationClass())
                .append(this.prefix, rhs.getPrefix())
                .append(this.properties, rhs.getProperties())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(implementationClass)
                .append(prefix)
                .append(properties)
                .toHashCode();
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
