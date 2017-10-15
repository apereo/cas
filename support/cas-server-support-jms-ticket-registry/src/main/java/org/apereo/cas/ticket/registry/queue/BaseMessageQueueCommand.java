package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * This is {@link BaseMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class BaseMessageQueueCommand {
    private final StringBean id;

    public BaseMessageQueueCommand(final StringBean id) {
        this.id = id;
    }

    public StringBean getId() {
        return id;
    }

    /**
     * Execute.
     *
     * @param registry the registry
     */
    public void execute(final TicketRegistry registry) {
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .toString();
    }
}
