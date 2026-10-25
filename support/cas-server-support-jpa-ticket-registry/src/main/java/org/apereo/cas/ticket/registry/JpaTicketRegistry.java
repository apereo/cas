package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.HibernateHints;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.2.1
 */
@Slf4j
@Getter
@Monitorable
public class JpaTicketRegistry extends AbstractTicketRegistry {

    private static final int DELETE_BATCH_SIZE = 500;

    private final JpaBeanFactory jpaBeanFactory;

    private final TransactionOperations transactionTemplate;

    private final CasConfigurationProperties casProperties;

    private final JpaTicketEntityFactory ticketEntityFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final EntityManager entityManager;

    public JpaTicketRegistry(final CipherExecutor cipherExecutor,
                             final TicketSerializationManager ticketSerializationManager,
                             final TicketCatalog ticketCatalog,
                             final ConfigurableApplicationContext applicationContext,
                             final JpaBeanFactory jpaBeanFactory,
                             final TransactionOperations transactionTemplate,
                             final CasConfigurationProperties casProperties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.jpaBeanFactory = jpaBeanFactory;
        this.transactionTemplate = transactionTemplate;
        this.casProperties = casProperties;
        this.ticketEntityFactory = new JpaTicketEntityFactory(casProperties.getTicket().getRegistry().getJpa().getDialect());
        this.entityManagerFactory = applicationContext.getBean("ticketEntityManagerFactory", EntityManagerFactory.class);
        this.entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory, null, true, EntityManager.class);
    }

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    /**
     * Marks the query read-only, so Hibernate neither keeps a snapshot of the rows it loads
     * nor checks them for changes when the transaction ends.
     *
     * @param <T>   the query type
     * @param query the query
     * @return the query
     */
    private static <T extends Query> T readOnly(final T query) {
        query.setHint(HibernateHints.HINT_READ_ONLY, true);
        return query;
    }

    /**
     * Marks a query that reads rows one batch at a time: read-only, with the configured fetch size. On MySQL the
     * persistence unit has no global fetch size, so this is what makes such a query, and only such a query, use
     * a server-side cursor instead of reading the whole result into memory.
     *
     * @param <T>   the query type
     * @param query the query
     * @return the query
     */
    private <T extends Query> T streaming(final T query) {
        query.setHint(HibernateHints.HINT_FETCH_SIZE, casProperties.getTicket().getRegistry().getJpa().getFetchSize());
        return readOnly(query);
    }

    private static String quoteLiteral(final String value) {
        return value.replace("'", "''");
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) {
        transactionTemplate.executeWithoutResult(Unchecked.consumer(status -> {
            val ticketEntity = getTicketEntityFrom(ticket);
            persistDetached(ticketEntity);
            LOGGER.debug("Added ticket [{}] to registry.", ticketEntity.getId());
        }));
        return ticket;
    }

    /**
     * Writes the entity and detaches it. Tickets are updated with a bulk statement that does not
     * touch the persistence context, so a managed copy left behind would be returned, unchanged,
     * by any later read in the same transaction.
     *
     * @param ticketEntity the ticket entity
     */
    private void persistDetached(final BaseTicketEntity ticketEntity) {
        entityManager.persist(ticketEntity);
        entityManager.flush();
        entityManager.detach(ticketEntity);
    }

    @Override
    public @Nullable Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return transactionTemplate.execute(callback -> {
            try {
                val encTicketId = digestIdentifier(ticketId);
                if (StringUtils.isNotBlank(encTicketId)) {

                    val sql = String.format("SELECT t FROM %s t WHERE t.id = :id", ticketEntityFactory.getEntityName());
                    val query = readOnly(entityManager.createQuery(sql, ticketEntityFactory.getType()));
                    query.setParameter("id", encTicketId);
                    query.setLockMode(getConfiguredLockModeType());
                    val entity = toDetachedTicket(query.getSingleResult());
                    val result = decodeTicket(entity);
                    return predicate.test(result) ? result : null;
                }
            } catch (final NoResultException e) {
                LOGGER.debug("No record could be found for ticket [{}]", ticketId);
            }
            return null;
        });
    }

    @Override
    public int deleteTicket(final String ticketId) {
        return transactionTemplate.execute(_ -> FunctionUtils.doUnchecked(() -> super.deleteTicket(ticketId)));
    }

    @Override
    public long deleteAll() {
        return transactionTemplate.execute(_ -> {
            val query = entityManager.createQuery(String.format("DELETE FROM %s", ticketEntityFactory.getEntityName()));
            return Long.valueOf(query.executeUpdate());
        });
    }

    @Override
    public long deleteTicketsFor(final String principalId) {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("DELETE FROM %s t WHERE t.principalId=:principalId", ticketEntityFactory.getEntityName());
            val query = entityManager.createQuery(sql)
                .setParameter("principalId", digestIdentifier(principalId));
            query.setLockMode(LockModeType.NONE);
            return Long.valueOf(query.executeUpdate());
        });
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("SELECT t FROM %s t", ticketEntityFactory.getEntityName());
            val query = streaming(entityManager.createQuery(sql, ticketEntityFactory.getType()));
            query.setLockMode(getConfiguredLockModeType());

            return query
                .getResultStream()
                .map(this::toDetachedTicket)
                .map(this::decodeTicket)
                .collect(Collectors.toList());
        });
    }

    /**
     * Rewrites the row with a single statement, where {@code merge} would first read it back. When no row
     * matches, the ticket is added instead, which is what {@code merge} did for a ticket that was not stored.
     * The ticket is encoded once, for both the row and the result.
     *
     * @param ticket the ticket
     * @return the encoded ticket
     */
    @Override
    public @Nullable Ticket updateTicket(final Ticket ticket) {
        return transactionTemplate.execute(_ -> FunctionUtils.doUnchecked(() -> {
            LOGGER.trace("Updating ticket [{}]", ticket);
            val encodedTicket = encodeTicket(ticket);
            val ticketEntity = getTicketEntityFrom(encodedTicket, ticket);
            val sql = String.format("UPDATE %s t SET t.body = :body, t.type = :type, t.parentId = :parentId, "
                + "t.principalId = :principalId, t.service = :service, t.attributes = :attributes, "
                + "t.creationTime = :creationTime, t.expirationTime = :expirationTime, t.lastUsedTime = :lastUsedTime "
                + "WHERE t.id = :id", ticketEntityFactory.getEntityName());
            val updated = entityManager.createQuery(sql)
                .setParameter("body", ticketEntity.getBody())
                .setParameter("type", ticketEntity.getType())
                .setParameter("parentId", ticketEntity.getParentId())
                .setParameter("principalId", ticketEntity.getPrincipalId())
                .setParameter("service", ticketEntity.getService())
                .setParameter("attributes", ticketEntity.getAttributes())
                .setParameter("creationTime", ticketEntity.getCreationTime())
                .setParameter("expirationTime", ticketEntity.getExpirationTime())
                .setParameter("lastUsedTime", ticketEntity.getLastUsedTime())
                .setParameter("id", ticketEntity.getId())
                .executeUpdate();
            if (updated == 0) {
                persistDetached(ticketEntity);
            }
            LOGGER.debug("Updated ticket [{}]", ticketEntity.getId());
            return encodedTicket;
        }));
    }

    /**
     * This method purposefully doesn't lock any rows, because the stream traversing can take an indeterminate
     * amount of time, and logging in to an application with an existing TGT will update the TGT row in the database.
     * The page is applied by the query, so rows before it are never read. When the criteria ask only for tickets
     * expiring before an instant, the indexed expiration time selects them, earliest first.
     *
     * @return streamable results
     */
    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        val expiringBefore = criteria.getExpiringBefore();
        val sql = expiringBefore == null
            ? String.format("SELECT t FROM %s t", ticketEntityFactory.getEntityName())
            : String.format("SELECT t FROM %s t WHERE t.expirationTime <= :expiringBefore ORDER BY t.expirationTime",
                ticketEntityFactory.getEntityName());
        val query = streaming(entityManager.createQuery(sql, ticketEntityFactory.getType()));
        query.setLockMode(LockModeType.NONE);
        if (expiringBefore != null) {
            query.setParameter("expiringBefore", expiringBefore);
        }
        if (criteria.getFrom() > 0) {
            query.setFirstResult((int) Math.min(criteria.getFrom(), Integer.MAX_VALUE));
        }
        if (!criteria.isInfiniteCount()) {
            query.setMaxResults((int) Math.min(criteria.getCount(), Integer.MAX_VALUE));
        }
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(this::toDetachedTicket)
            .map(this::decodeTicket);
    }

    /**
     * Rows read inside a transaction stay in its persistence context until it ends, so a scan of the
     * whole table would otherwise keep every row in memory, and a later read in the same transaction
     * would be handed the copy loaded before a bulk update. Outside a transaction each query has its own
     * short-lived context and there is nothing to detach from.
     *
     * @param entity the entity
     * @return the ticket
     */
    private Ticket toDetachedTicket(final BaseTicketEntity entity) {
        val ticket = ticketEntityFactory.toTicket(entity);
        if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
            entityManager.detach(entity);
        }
        return ticket;
    }

    @Override
    public long sessionCount() {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", ticketEntityFactory.getEntityName());
            val query = entityManager.createQuery(sql).setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
            return countToLong(query.getSingleResult());
        });
    }

    @Override
    public long countTickets() {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("SELECT COUNT(t.id) FROM %s t", ticketEntityFactory.getEntityName());
            val query = entityManager.createQuery(sql);
            return countToLong(query.getSingleResult());
        });
    }

    /**
     * Counted by the indexed query {@link #getSessionsFor(String)} runs, rather than by the inherited
     * version, which reads and decodes every ticket in the table through a cursor it never closes.
     * <p>
     * The cursor is consumed inside the transaction because that is what keeps the JDBC connection,
     * and therefore the result set, open for the length of the traversal. Consuming it outside one
     * leaves the connection free to return to the pool before the first row is read.
     *
     * @param principalId the principal id
     * @return the number of sessions held for the principal
     */
    @Override
    public long countSessionsFor(final String principalId) {
        return transactionTemplate.execute(_ -> {
            try (val sessions = getSessionsFor(principalId)) {
                return sessions.count();
            }
        });
    }

    @Override
    public Stream<? extends Ticket> getTicketsFor(final Service service) {
        val sql = String.format("SELECT t FROM %s t WHERE t.service=:service", ticketEntityFactory.getEntityName());
        val query = streaming(entityManager.createQuery(sql, ticketEntityFactory.getType())).setParameter("service", service.getId());
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(this::toDetachedTicket)
            .map(this::decodeTicket)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }
    
    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val sql = String.format("SELECT t FROM %s t WHERE t.type=:type AND t.principalId=:principalId", ticketEntityFactory.getEntityName());
        val query = streaming(entityManager.createQuery(sql, ticketEntityFactory.getType()))
            .setParameter("principalId", digestIdentifier(principalId))
            .setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(this::toDetachedTicket)
            .map(this::decodeTicket)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public List<? extends Serializable> query(final TicketRegistryQueryCriteria criteria) {
        var sql = String.format("SELECT t FROM %s t WHERE t.type=:type", ticketEntityFactory.getEntityName());
        if (StringUtils.isNotBlank(criteria.getId())) {
            sql = sql.concat(" AND t.id = :id");
        }
        if (StringUtils.isNotBlank(criteria.getPrincipal())) {
            sql = sql.concat(" AND t.principalId = :principalId");
        }
        val definition = ticketCatalog.find(criteria.getType());
        val query = streaming(entityManager.createQuery(sql, ticketEntityFactory.getType()));
        query.setParameter("type", getTicketTypeName(definition.getApiClass()));
        if (StringUtils.isNotBlank(criteria.getId())) {
            query.setParameter("id", digestIdentifier(criteria.getId()));
        }
        if (StringUtils.isNotBlank(criteria.getPrincipal())) {
            query.setParameter("principalId", digestIdentifier(criteria.getPrincipal()));
        }
        if (criteria.getCount() > 0) {
            query.setMaxResults(Long.valueOf(criteria.getCount()).intValue());
        }
        query.setLockMode(LockModeType.NONE);

        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(this::toDetachedTicket)
            .map(ticket -> criteria.isDecode() ? decodeTicket(ticket) : ticket)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Finds sessions that hold, for every given attribute, at least one of its values. The ticket type and
     * the values are bound as parameters, so the statement only changes with the attribute names. Those are
     * part of a JSON path, where the databases accept no parameter (Postgres excepted), and are written as
     * quoted literals. The matches are collected inside a transaction, which keeps the connection, and with it
     * the result set, open while the rows are read; callers then work on the collected sessions and need no
     * transaction of their own.
     *
     * @param queryAttributes the attributes to match
     * @return the matching sessions
     */
    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val parameters = new LinkedHashMap<String, Object>();
        parameters.put("type", getTicketTypeName(TicketGrantingTicket.class));
        val attributes = List.copyOf(queryAttributes.entrySet());
        val criteria = IntStream.range(0, attributes.size())
            .mapToObj(index -> {
                val entry = attributes.get(index);
                val values = entry.getValue();
                return IntStream.range(0, values.size())
                    .mapToObj(valueIndex -> {
                        val valueParameter = "v%d_%d".formatted(index, valueIndex);
                        parameters.put(valueParameter, digestIdentifier(values.get(valueIndex).toString()));
                        return toAttributeCriterion(index, entry.getKey(), valueParameter, parameters);
                    })
                    .collect(Collectors.joining(" OR ", "(", ")"));
            })
            .collect(Collectors.joining(" AND "));

        val selectClause = new StringBuilder(String.format("SELECT t.* FROM %s t ", ticketEntityFactory.getTableName()));
        if (ticketEntityFactory.isMsSqlServer()) {
            for (var index = 0; index < attributes.size(); index++) {
                val name = quoteLiteral(digestIdentifier(attributes.get(index).getKey()));
                selectClause.append(String.format("CROSS APPLY OPENJSON(t.attributes, '$.\"%s\"') a%d ", name, index));
            }
        }

        val sql = String.format("%s WHERE t.type = :type AND %s", selectClause, criteria);
        LOGGER.debug("Executing SQL query [{}]", sql);
        val sessions = transactionTemplate.execute(_ -> {
            val query = streaming(entityManager.createNativeQuery(sql, ticketEntityFactory.getType()));
            parameters.forEach(query::setParameter);
            try (val rows = jpaBeanFactory.streamQuery(query)) {
                return rows
                    .map(BaseTicketEntity.class::cast)
                    .map(this::toDetachedTicket)
                    .map(this::decodeTicket)
                    .filter(Objects::nonNull)
                    .filter(ticket -> !ticket.isExpired())
                    .toList();
            }
        });
        return Objects.requireNonNullElse(sessions, List.<Ticket>of()).stream();
    }

    /**
     * Builds the condition that the attribute holds the value bound to the given parameter.
     * On SQL Server the attribute is opened by the {@code CROSS APPLY} aliased {@code a<index>}.
     *
     * @param index          the position of the attribute in the query
     * @param name           the attribute name
     * @param valueParameter the parameter that holds the value
     * @param parameters     the query parameters, to which a parameter for the name is added when it can be bound
     * @return the condition
     */
    private String toAttributeCriterion(final int index, final String name, final String valueParameter,
                                        final Map<String, Object> parameters) {
        if (ticketEntityFactory.isOracle()) {
            return String.format("JSON_EXISTS(t.attributes, '$?(@.\"%s\" == $%s)' PASSING :%s AS \"%s\")",
                quoteLiteral(digestIdentifier(name)), valueParameter, valueParameter, valueParameter);
        }
        if (ticketEntityFactory.isPostgres()) {
            val nameParameter = "k" + index;
            parameters.put(nameParameter, digestIdentifier(name));
            return String.format("jsonb_exists(cast(t.attributes->:%s as jsonb), :%s)", nameParameter, valueParameter);
        }
        if (ticketEntityFactory.isMariaDb()) {
            val attributeKey = isCipherExecutorEnabled() ? name : name.replace("-", "\\\\-");
            return String.format("JSON_SEARCH(JSON_QUERY(t.attributes, '$.\"%s\"'), 'one', :%s) is not null",
                quoteLiteral(digestIdentifier(attributeKey)), valueParameter);
        }
        if (ticketEntityFactory.isMySql()) {
            return String.format("JSON_SEARCH(JSON_EXTRACT(t.attributes, '$.\"%s\"'), 'one', :%s) is not null",
                quoteLiteral(digestIdentifier(name)), valueParameter);
        }
        if (ticketEntityFactory.isMsSqlServer()) {
            return String.format("a%d.value = :%s", index, valueParameter);
        }
        return "1=2";
    }

    @Override
    public long countTicketsFor(final Service service) {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.service = :service", ticketEntityFactory.getEntityName());
            val query = entityManager.createQuery(sql).setParameter("service", service.getId());
            return countToLong(query.getSingleResult());
        });
    }

    protected String getTicketTypeName(final Class<? extends Ticket> clazz) {
        return isCipherExecutorEnabled()
            ? DefaultEncodedTicket.class.getName()
            : ticketCatalog.findTicketDefinition(clazz).orElseThrow().getImplementationClass().getName();
    }

    @Override
    public long serviceTicketCount() {
        return transactionTemplate.execute(_ -> {
            val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", ticketEntityFactory.getEntityName());
            val query = entityManager.createQuery(sql)
                .setParameter("type", getTicketTypeName(ServiceTicket.class));
            return countToLong(query.getSingleResult());
        });
    }
    
    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val result = transactionTemplate.execute(transactionStatus -> {
            val encTicketId = digestIdentifier(ticketToDelete.getId());
            var totalCount = 0;
            if (isCascadeRemovals(ticketToDelete)) {
                totalCount = deleteTicketGrantingTickets(encTicketId);
            } else {
                val sql = String.format("DELETE FROM %s o WHERE o.id = :id", ticketEntityFactory.getEntityName());
                val query = entityManager.createQuery(sql);
                query.setParameter("id", encTicketId);
                totalCount = query.executeUpdate();
            }
            return totalCount;
        });
        return result != null ? result : 0L;
    }

    protected BaseTicketEntity getTicketEntityFrom(final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> getTicketEntityFrom(encodeTicket(ticket), ticket));
    }

    /**
     * Builds the entity for a ticket that is already encoded, so a caller that also needs the encoded ticket
     * encodes it only once.
     *
     * @param encodedTicket the encoded ticket
     * @param ticket        the ticket
     * @return the ticket entity
     */
    protected BaseTicketEntity getTicketEntityFrom(final Ticket encodedTicket, final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> {
            val ticketEntity = ticketEntityFactory
                .fromTicket(encodedTicket, ticket)
                .setPrincipalId(digestIdentifier(getPrincipalIdFrom(ticket)))
                .setAttributes(collectAndDigestTicketAttributes(ticket));
            if (ticket instanceof final TicketGrantingTicketAwareTicket grantingTicketAware && grantingTicketAware.getTicketGrantingTicket() != null) {
                ticketEntity.setParentId(digestIdentifier(grantingTicketAware.getTicketGrantingTicket().getId()));
            }
            return ticketEntity;
        });
    }

    /**
     * Service tickets are stored with their ticket-granting ticket as parent. When removals cascade,
     * {@link #deleteSingleTicket(Ticket)} removes them with the rest of the ticket's children, and
     * removing them here first would only run the same deletes twice.
     *
     * @param ticket the ticket-granting ticket
     * @return the number of service tickets removed here
     */
    @Override
    protected int deleteServiceTickets(final TicketGrantingTicket ticket) {
        if (isCascadeRemovals(ticket)) {
            return 0;
        }
        val services = ticket.getServices();
        if (services == null || services.isEmpty()) {
            return 0;
        }
        val ticketIds = services.keySet().stream().map(this::digestIdentifier).toList();
        val sql = String.format("DELETE FROM %s t WHERE t.id IN :ids", ticketEntityFactory.getEntityName());
        val result = transactionTemplate.execute(_ -> {
            var count = 0;
            for (var i = 0; i < ticketIds.size(); i += DELETE_BATCH_SIZE) {
                val batch = ticketIds.subList(i, Math.min(i + DELETE_BATCH_SIZE, ticketIds.size()));
                count += entityManager.createQuery(sql).setParameter("ids", batch).executeUpdate();
            }
            return count;
        });
        LOGGER.debug("Removed [{}] ticket(s) issued by [{}]", result, ticket.getId());
        return result != null ? result : 0;
    }

    /*
     * An OR across two columns can seek neither index, so a single statement could scan the table and take locks
     * on rows belonging to other sessions. Now each half seeks its own index. Children go first so that no row is left
     * pointing at a parent that is already gone, and the total returned is unchanged.
     */
    protected int deleteTicketGrantingTickets(final String ticketId) {
        return transactionTemplate.execute(_ -> {
            val children = deleteTicketsBy("t.parentId", ticketId);
            val parent = deleteTicketsBy("t.id", ticketId);
            return children + parent;
        });
    }

    private int deleteTicketsBy(final String column, final String ticketId) {
        val sql = String.format("DELETE FROM %s t WHERE %s = :id", ticketEntityFactory.getEntityName(), column);
        LOGGER.trace("Creating delete query [{}] for ticket id [{}]", sql, ticketId);
        val query = entityManager.createQuery(sql);
        query.setParameter("id", ticketId);
        return query.executeUpdate();
    }

    private boolean isCascadeRemovals(final Ticket ticket) {
        val definition = ticketCatalog.find(ticket);
        return definition != null && definition.getProperties().isCascadeRemovals();
    }

    protected LockModeType getConfiguredLockModeType() {
        val lockTypeName = casProperties.getTicket().getRegistry().getJpa().getTicketLockType();
        return LockModeType.valueOf(lockTypeName);
    }
}
