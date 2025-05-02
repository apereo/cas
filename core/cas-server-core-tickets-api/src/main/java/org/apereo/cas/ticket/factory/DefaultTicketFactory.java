package org.apereo.cas.ticket.factory;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link DefaultTicketFactory} is responsible for creating ticket factory objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@NoArgsConstructor
public class DefaultTicketFactory implements TicketFactory {
    private final Map<String, Object> factoryMap = new HashMap<>();

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return (TicketFactory) this.factoryMap.get(clazz.getCanonicalName());
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return Ticket.class;
    }

    /**
     * Add ticket factory.
     *
     * @param ticketClass the ticket class
     * @param factory     the factory
     * @return the default ticket factory
     */
    @CanIgnoreReturnValue
    public DefaultTicketFactory addTicketFactory(final @NonNull Class<? extends Ticket> ticketClass,
                                                 final @NonNull TicketFactory factory) {
        this.factoryMap.put(ticketClass.getCanonicalName(), factory);
        return this;
    }

    @Override
    public ExpirationPolicyBuilder getExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {

            @Serial
            private static final long serialVersionUID = -8720633582482747264L;

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return AlwaysExpiresExpirationPolicy.INSTANCE;
            }
        };
    }
}
