package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link JpaTicketEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Table(name = "CasTickets")
@Entity(name = JpaTicketEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaTicketEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaTicketEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    private static TicketSerializationManager TICKET_SERIALIZATION_MANAGER;

    @Column(nullable = false, length = 8192)
    private String body;

    @Column(nullable = false, length = 768)
    @Id
    private String id;

    @Column(length = 1024)
    private String parentId;

    @Column(length = 1024)
    private String principalId;

    @Column(nullable = false, length = 1024)
    private String type;

    @Column(name = "CREATION_TIME", length = 512)
    private ZonedDateTime creationTime;

    private static TicketSerializationManager getInstance() {
        if (TICKET_SERIALIZATION_MANAGER == null) {
            TICKET_SERIALIZATION_MANAGER = ApplicationContextProvider.getApplicationContext().getBean(TicketSerializationManager.class);
        }
        return TICKET_SERIALIZATION_MANAGER;
    }

    /**
     * From.
     *
     * @param ticket the ticket
     * @return the jpa ticket entity
     */
    public static JpaTicketEntity fromTicket(final Ticket ticket) {
        val jsonBody = getInstance().serializeTicket(ticket);
        val authentication = ticket instanceof AuthenticationAwareTicket
            ? ((AuthenticationAwareTicket) ticket).getAuthentication()
            : null;
        return JpaTicketEntity.builder()
            .id(ticket.getId())
            .parentId(ticket.getTicketGrantingTicket() != null ? ticket.getTicketGrantingTicket().getId() : null)
            .body(jsonBody)
            .type(ticket.getClass().getName())
            .principalId(authentication != null ? authentication.getPrincipal().getId() : null)
            .creationTime(ticket.getCreationTime())
            .build();
    }

    /**
     * To registered service.
     *
     * @return the registered service
     */
    public Ticket toTicket() {
        val ticket = getInstance().deserializeTicket(this.body, this.type);
        LOGGER.trace("Converted JPA entity [{}] to [{}]", this, ticket);
        return ticket;
    }
}
