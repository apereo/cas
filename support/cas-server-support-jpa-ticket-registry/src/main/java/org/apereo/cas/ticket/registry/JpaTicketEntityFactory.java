package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
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
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Strings;
import jakarta.persistence.Table;

/**
 * This is {@link JpaTicketEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class JpaTicketEntityFactory extends AbstractJpaEntityFactory<BaseTicketEntity> {
    /**
     * Connector/J settings that let MySQL stream a scan through a server-side cursor. A cursor is used for
     * statements with a fetch size above zero; Connector/J then prepares every statement on the server, so
     * prepared statements are cached per connection to keep lookups at one round trip.
     */
    private static final Map<String, String> MYSQL_DRIVER_DEFAULTS = Map.of(
        "useCursorFetch", "true",
        "cachePrepStmts", "true",
        "prepStmtCacheSize", "250",
        "prepStmtCacheSqlLimit", "2048");

    private static final int UNKNOWN_EXPIRATION_IN_YEARS = 100;

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

    /**
     * The settings to build the ticket registry's data source and persistence unit with. On MySQL, the Connector/J
     * defaults declared on this class are added unless the properties or the URL already set them, and
     * the persistence unit gets no global fetch size: only the queries that stream rows set one, so that only
     * they use a cursor. The given settings are left untouched; for other databases they are returned as is.
     *
     * @param properties the ticket registry settings
     * @return the settings to use
     */
    public JpaTicketRegistryProperties withDialectDefaults(final JpaTicketRegistryProperties properties) {
        if (!isMySql()) {
            return properties;
        }
        val effective = SerializationUtils.clone(properties);
        effective.setFetchSize(0);
        MYSQL_DRIVER_DEFAULTS.forEach((name, value) -> {
            if (!Strings.CI.contains(properties.getUrl(), name)) {
                effective.getProperties().putIfAbsent(name, value);
            }
        });
        return effective;
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

        val expirationTime = toExpirationTime(realTicket);
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

    /**
     * The time the ticket expires unless it is used again: now for a ticket that is already expired, such as one
     * marked expired or out of uses, otherwise the earlier of its maximum and its idle expiration time. Rows are
     * rewritten on every update, which is when the last-used time behind the idle expiration moves. A policy that
     * gives neither time is stored as expiring far in the future and left to the cleaner's periodic full scan.
     *
     * @param ticket the ticket
     * @return the expiration time
     */
    static ZonedDateTime toExpirationTime(final Ticket ticket) {
        val now = ZonedDateTime.now(Clock.systemUTC());
        if (ticket.isExpired()) {
            return now;
        }
        val policy = ticket.getExpirationPolicy();
        return Stream.of(policy.toMaximumExpirationTime(ticket), toIdleExpirationTime(policy, ticket))
            .filter(Objects::nonNull)
            .min(Comparator.comparing(ZonedDateTime::toInstant))
            .orElseGet(() -> now.plusYears(UNKNOWN_EXPIRATION_IN_YEARS));
    }

    private static ZonedDateTime toIdleExpirationTime(final ExpirationPolicy policy, final Ticket ticket) {
        if (policy instanceof final BaseDelegatingExpirationPolicy delegating
            && ticket instanceof final AuthenticationAwareTicket authenticationAware) {
            return delegating.getExpirationPolicyFor(authenticationAware)
                .map(delegate -> toIdleExpirationTime(delegate, ticket))
                .orElse(null);
        }
        return policy instanceof final IdleExpirationPolicy idle ? idle.getIdleExpirationTime(ticket) : null;
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
        return RelaxedPropertyNames.NameManipulations.CASE_INSENSITIVE_CAMELCASE_TO_UNDERSCORE.apply(tableName);
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
