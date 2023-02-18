package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
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
import org.jooq.lambda.Unchecked;
import org.springframework.transaction.support.TransactionOperations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class JpaTicketRegistry extends AbstractTicketRegistry {

    private final JpaBeanFactory jpaBeanFactory;

    private final TransactionOperations transactionTemplate;

    private final CasConfigurationProperties casProperties;

    @PersistenceContext(unitName = "jpaTicketRegistryContext")
    private EntityManager entityManager;

    public JpaTicketRegistry(final CipherExecutor cipherExecutor,
                             final TicketSerializationManager ticketSerializationManager,
                             final TicketCatalog ticketCatalog, final JpaBeanFactory jpaBeanFactory,
                             final TransactionOperations transactionTemplate,
                             final CasConfigurationProperties casProperties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.jpaBeanFactory = jpaBeanFactory;
        this.transactionTemplate = transactionTemplate;
        this.casProperties = casProperties;
    }

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        transactionTemplate.executeWithoutResult(Unchecked.consumer(status -> {
            val ticketEntity = getTicketEntityFrom(ticket);
            if (ticket instanceof TicketGrantingTicketAwareTicket grantingTicketAware && grantingTicketAware.getTicketGrantingTicket() != null) {
                val parentId = digest(grantingTicketAware.getTicketGrantingTicket().getId());
                ticketEntity.setParentId(parentId);
            }
            entityManager.persist(ticketEntity);
            LOGGER.debug("Added ticket [{}] to registry.", ticketEntity.getId());
        }));
    }

    protected BaseTicketEntity getTicketEntityFrom(final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> {
            val encodeTicket = encodeTicket(ticket);
            return getJpaTicketEntityFactory()
                .fromTicket(encodeTicket)
                .setPrincipalId(digest(getPrincipalIdFrom(ticket)))
                .setAttributes(collectAndDigestTicketAttributes(ticket));
        });
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return transactionTemplate.execute(callback -> {
            try {
                val encTicketId = digest(ticketId);
                if (StringUtils.isNotBlank(encTicketId)) {
                    val factory = getJpaTicketEntityFactory();
                    val sql = String.format("SELECT t FROM %s t WHERE t.id = :id", factory.getEntityName());
                    val query = entityManager.createQuery(sql, factory.getType());
                    query.setParameter("id", encTicketId);
                    query.setLockMode(casProperties.getTicket().getRegistry().getJpa().getTicketLockType());
                    val ticket = query.getSingleResult();
                    val entity = getJpaTicketEntityFactory().toTicket(ticket);
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
        return transactionTemplate.execute(callback -> FunctionUtils.doUnchecked(() -> super.deleteTicket(ticketId)));
    }

    @Override
    public long deleteAll() {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val query = entityManager.createQuery(String.format("DELETE FROM %s", factory.getEntityName()));
            return Long.valueOf(query.executeUpdate());
        });
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val sql = String.format("SELECT t FROM %s t", factory.getEntityName());
            val query = entityManager.createQuery(sql, factory.getType());
            query.setLockMode(casProperties.getTicket().getRegistry().getJpa().getTicketLockType());

            return query
                .getResultStream()
                .map(factory::toTicket)
                .map(this::decodeTicket)
                .collect(Collectors.toList());
        });
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        return transactionTemplate.execute(status -> FunctionUtils.doUnchecked(() -> {
            LOGGER.trace("Updating ticket [{}]", ticket);
            val ticketEntity = getTicketEntityFrom(ticket);
            entityManager.merge(ticketEntity);
            LOGGER.debug("Updated ticket [{}]", ticketEntity.getId());
            return encodeTicket(ticket);
        }));
    }

    /**
     * This method purposefully doesn't lock any rows, because the stream traversing can take an indeterminate
     * amount of time, and logging in to an application with an existing TGT will update the TGT row in the database.
     *
     * @return streamable results
     */
    @Override
    public Stream<? extends Ticket> stream() {
        val factory = getJpaTicketEntityFactory();
        val sql = String.format("SELECT t FROM %s t", factory.getEntityName());
        val query = entityManager.createQuery(sql, factory.getType());
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(factory::toTicket)
            .map(this::decodeTicket);
    }

    @Override
    public long sessionCount() {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", factory.getEntityName());
            val query = entityManager.createQuery(sql).setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
            return countToLong(query.getSingleResult());
        });
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val factory = getJpaTicketEntityFactory();
        val sql = String.format("SELECT t FROM %s t WHERE t.type=:type AND t.principalId=:principalId", factory.getEntityName());
        val query = entityManager.createQuery(sql, factory.getType())
            .setParameter("principalId", digest(principalId))
            .setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(factory::toTicket)
            .map(this::decodeTicket)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val factory = getJpaTicketEntityFactory();
        val criterias = queryAttributes.entrySet()
            .stream()
            .map(entry -> {
                val criteriaValues = entry.getValue()
                    .stream()
                    .map(queryValue -> {
                        if (factory.isOracle()) {
                            return String.format("JSON_EXISTS(t.attributes, '$?(@.\"%s\" == \"%s\")')",
                                digest(entry.getKey()), digest(queryValue.toString()));
                        }
                        if (factory.isPostgres()) {
                            return String.format("(t.attributes->'%s')\\:\\:jsonb \\?\\? '%s'", digest(entry.getKey()), digest(queryValue.toString()));
                        }
                        if (factory.isMariaDb()) {
                            val attributeKey = isCipherExecutorEnabled()
                                ? entry.getKey()
                                : entry.getKey().replace("-", "\\\\-");
                            return String.format("JSON_SEARCH(JSON_QUERY(t.attributes, '$.%s'), 'one', '%s') is not null",
                                String.format("\"%s\"", digest(attributeKey)), digest(queryValue.toString()));
                        }
                        if (factory.isMySql()) {
                            return String.format("JSON_SEARCH(JSON_EXTRACT(t.attributes, '$.%s'), 'one', '%s') is not null",
                                String.format("\"%s\"", digest(entry.getKey())), digest(queryValue.toString()));
                        }
                        if (factory.isMsSqlServer()) {
                            return String.format("%s.value='%s'", String.format("\"%s\"", digest(entry.getKey())), digest(queryValue.toString()));
                        }
                        return "1=2";
                    })
                    .collect(Collectors.joining(" OR "));
                return String.format("(%s)", criteriaValues);
            })
            .collect(Collectors.joining(" AND "));

        val selectClause = new StringBuilder(String.format("SELECT t.* FROM %s t ", factory.getTableName()));
        if (factory.isMsSqlServer()) {
            for (val entry : queryAttributes.entrySet()) {
                val name = String.format("\"%s\"", digest(entry.getKey()));
                selectClause.append(String.format("CROSS APPLY OPENJSON(t.attributes, '$.%s') %s ", name, name));
            }
        }

        val sql = String.format("%s WHERE t.type='%s' AND %s", selectClause,
            getTicketTypeName(TicketGrantingTicket.class), criterias);
        LOGGER.debug("Executing SQL query [{}]", sql);
        entityManager.flush();
        val query = entityManager.createNativeQuery(sql, factory.getType());
        return jpaBeanFactory.streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(factory::toTicket)
            .map(this::decodeTicket)
            .filter(ticket -> !ticket.isExpired());
    }

    protected String getTicketTypeName(final Class<? extends Ticket> clazz) {
        return isCipherExecutorEnabled()
            ? DefaultEncodedTicket.class.getName()
            : ticketCatalog.findTicketDefinition(clazz).orElseThrow().getImplementationClass().getName();
    }

    @Override
    public long serviceTicketCount() {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", factory.getEntityName());
            val query = entityManager.createQuery(sql)
                .setParameter("type", getTicketTypeName(ServiceTicket.class));
            return countToLong(query.getSingleResult());
        });
    }

    @Override
    public long deleteSingleTicket(final String ticketIdToDelete) {
        val result = transactionTemplate.execute(transactionStatus -> {
            val factory = getJpaTicketEntityFactory();
            val encTicketId = digest(ticketIdToDelete);
            var totalCount = 0;
            val md = ticketCatalog.find(ticketIdToDelete);

            if (md.getProperties().isCascadeRemovals()) {
                totalCount = deleteTicketGrantingTickets(encTicketId);
            } else {
                val sql = String.format("DELETE FROM %s o WHERE o.id = :id", factory.getEntityName());
                val query = entityManager.createQuery(sql);
                query.setParameter("id", encTicketId);
                totalCount = query.executeUpdate();
            }
            return totalCount;
        });
        return Objects.requireNonNull(result);
    }

    protected JpaTicketEntityFactory getJpaTicketEntityFactory() {
        val jpa = casProperties.getTicket().getRegistry().getJpa();
        return new JpaTicketEntityFactory(jpa.getDialect());
    }

    protected int deleteTicketGrantingTickets(final String ticketId) {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            var sql = String.format("DELETE FROM %s t WHERE t.parentId = :id OR t.id = :id", factory.getEntityName());
            LOGGER.trace("Creating delete query [{}] for ticket id [{}]", sql, ticketId);
            var query = entityManager.createQuery(sql);
            query.setParameter("id", ticketId);
            return query.executeUpdate();
        });
    }
}
