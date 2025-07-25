package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.apereo.cas.ticket.registry.generic.JpaTicketEntity;
import org.apereo.cas.ticket.registry.mssql.MsSqlServerJpaTicketEntity;
import org.apereo.cas.ticket.registry.mysql.MySQLJpaTicketEntity;
import org.apereo.cas.ticket.registry.oracle.OracleJpaTicketEntity;
import org.apereo.cas.ticket.registry.postgres.PostgresJpaTicketEntity;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link JpaTicketEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class JpaTicketEntityFactory extends AbstractJpaEntityFactory<BaseTicketEntity> {

    public JpaTicketEntityFactory(final String dialect) {
        super(dialect);
    }

    private static final class ThreadSafeHolder {
        private static final TicketSerializationManager TICKET_SERIALIZATION_MANAGER =
            ApplicationContextProvider.getApplicationContext().getBean(TicketSerializationManager.class);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static TicketSerializationManager getTicketSerializationManager() {
        return ThreadSafeHolder.TICKET_SERIALIZATION_MANAGER;
    }

    public String getEntityName() {
        return getEntityClass().getSimpleName();
    }

    /**
     * From tickets objects to entity.
     *
     * @param encodedTicket the ticket
     * @param realTicket    the real ticket
     * @return the jpa ticket entity
     */
    public BaseTicketEntity fromTicket(final Ticket encodedTicket, final Ticket realTicket) {
        val jsonBody = getTicketSerializationManager().serializeTicket(encodedTicket);
        val authentication = encodedTicket instanceof final AuthenticationAwareTicket authAware
            ? authAware.getAuthentication()
            : null;

        val parentTicket = encodedTicket instanceof final TicketGrantingTicketAwareTicket tgtAware
            ? tgtAware.getTicketGrantingTicket()
            : null;

        val expirationTime = realTicket.getExpirationPolicy().toMaximumExpirationTime(realTicket);
        val entity = FunctionUtils.doUnchecked(() -> getEntityClass().getDeclaredConstructor().newInstance());
        return entity
            .setId(encodedTicket.getId())
            .setParentId(Optional.ofNullable(parentTicket).map(Ticket::getId).orElse(null))
            .setBody(jsonBody)
            .setType(encodedTicket.getClass().getName())
            .setService(realTicket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? sat.getService().getId() : null)
            .setPrincipalId(Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .map(Principal::getId)
                .orElse(null))
            .setExpirationTime(expirationTime)
            .setLastUsedTime(ObjectUtils.getIfNull(encodedTicket.getLastTimeUsed(), ZonedDateTime.now(Clock.systemUTC())))
            .setCreationTime(ObjectUtils.getIfNull(encodedTicket.getCreationTime(), ZonedDateTime.now(Clock.systemUTC())));
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

    /**
     * Gets table name.
     *
     * @return the table name
     */
    public String getTableName() {
        val tableName = getType().getAnnotation(Table.class).name();
        return RelaxedPropertyNames.NameManipulations.CAMELCASE_TO_UNDERSCORE_TITLE_CASE.apply(tableName);
    }

    private Class<? extends BaseTicketEntity> getEntityClass() {
        if (isOracle()) {
            return OracleJpaTicketEntity.class;
        }
        if (isMySql()) {
            return MySQLJpaTicketEntity.class;
        }
        if (isPostgres()) {
            return PostgresJpaTicketEntity.class;
        }
        if (isMsSqlServer()) {
            return MsSqlServerJpaTicketEntity.class;
        }
        return JpaTicketEntity.class;
    }
}
