package org.apereo.cas.ticket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.annotation.Nonnull;
import java.io.Serial;
import java.util.Comparator;

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
@RequiredArgsConstructor
public class DefaultTicketDefinition implements TicketDefinition {

    @Serial
    private static final long serialVersionUID = 1607439557834230284L;

    private final Class<? extends Ticket> implementationClass;

    private final Class<? extends Ticket> apiClass;

    private final String prefix;

    private final TicketDefinitionProperties properties = new DefaultTicketDefinitionProperties();

    private final int order;

    @Override
    public int compareTo(@Nonnull final TicketDefinition definition) {
        return Comparator
            .comparing(TicketDefinition::getPrefix)
            .thenComparing(TicketDefinition::getImplementationClass, Comparator.comparing(Class::getName))
            .thenComparing(TicketDefinition::getApiClass, Comparator.comparing(Class::getName))
            .compare(this, definition);
    }
}
