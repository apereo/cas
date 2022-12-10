package org.apereo.cas.ticket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;

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

    private final Class<? extends Ticket> implementationClass;

    private final Class<? extends Ticket> apiClass;

    private final String prefix;

    private final TicketDefinitionProperties properties = new DefaultTicketDefinitionProperties();

    private final int order;

    @Override
    public int compareTo(final TicketDefinition o) {
        return new CompareToBuilder()
            .append(this.prefix, o.getPrefix())
            .append(this.implementationClass, o.getImplementationClass())
            .append(this.apiClass, o.getApiClass())
            .build();
    }
}
