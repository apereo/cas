package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.ToString;
import lombok.Getter;

/**
 * This is {@link BaseMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@ToString
@Getter
@AllArgsConstructor
public abstract class BaseMessageQueueCommand {

    private final StringBean id;
    
    /**
     * Execute.
     *
     * @param registry the registry
     */
    public void execute(final TicketRegistry registry) {
    }
}
