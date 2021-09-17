package org.apereo.cas.ticket.registry;

import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.apereo.cas.ticket.registry.generic.JpaTicketEntity;
import org.apereo.cas.ticket.registry.mysql.MySQLJpaTicketEntity;
import org.apereo.cas.ticket.registry.postgres.PostgresJpaTicketEntity;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * This is {@link JpaTicketEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class JpaTicketEntityFactory extends AbstractJpaEntityFactory<BaseTicketEntity> {
    private static TicketSerializationManager TICKET_SERIALIZATION_MANAGER;

    public JpaTicketEntityFactory(final String dialect) {
        super(dialect);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static TicketSerializationManager getTicketSerializationManager() {
        if (TICKET_SERIALIZATION_MANAGER == null) {
            TICKET_SERIALIZATION_MANAGER = ApplicationContextProvider.getApplicationContext().getBean(TicketSerializationManager.class);
        }
        return TICKET_SERIALIZATION_MANAGER;
    }

    public String getEntityName() {
        return getEntityClass().getSimpleName();
    }

    /**
     * From.
     *
     * @param ticket the ticket
     * @return the jpa ticket entity
     */
    @SneakyThrows
    public BaseTicketEntity fromTicket(final Ticket ticket) {
        val jsonBody = getTicketSerializationManager().serializeTicket(ticket);
        val authentication = ticket instanceof AuthenticationAwareTicket
            ? ((AuthenticationAwareTicket) ticket).getAuthentication()
            : null;

        val entity = getEntityClass().getDeclaredConstructor().newInstance();
        return entity
            .setId(ticket.getId())
            .setParentId(ticket.getTicketGrantingTicket() != null ? ticket.getTicketGrantingTicket().getId() : null)
            .setBody(jsonBody)
            .setType(ticket.getClass().getName())
            .setPrincipalId(authentication != null ? authentication.getPrincipal().getId() : null)
            .setCreationTime(ObjectUtils.defaultIfNull(ticket.getCreationTime(), ZonedDateTime.now(Clock.systemUTC())));
    }

    @Override
    public Class<BaseTicketEntity> getType() {
        return (Class<BaseTicketEntity>) getEntityClass();
    }

    /**
     * To registered service.
     *
     * @param entity the entity
     * @return the registered service
     */
    public Ticket toTicket(final BaseTicketEntity entity) {
        val ticket = getTicketSerializationManager().deserializeTicket(entity.getBody(), entity.getType());
        LOGGER.trace("Converted JPA entity [{}] to [{}]", this, ticket);
        return ticket;
    }

    private Class<? extends BaseTicketEntity> getEntityClass() {
        if (isMySql()) {
            return MySQLJpaTicketEntity.class;
        }
        if (isPostgres()) {
            return PostgresJpaTicketEntity.class;
        }
        return JpaTicketEntity.class;
    }

}
