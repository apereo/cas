package org.apereo.cas.ticket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.Ordered;

/**
 * This is {@link DefaultTicketDefinition}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
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
    public DefaultTicketDefinition(final Class<? extends Ticket> implementationClass, final String prefix, final int order) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.order = order;
    }

    public DefaultTicketDefinition(final Class<? extends Ticket> implementationClass, final String prefix) {
        this(implementationClass, prefix, Ordered.LOWEST_PRECEDENCE);
    }
}
