package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Getter
public class JpaTicketRegistry extends AbstractTicketRegistry {
    private final LockModeType lockType;

    private final TicketCatalog ticketCatalog;

    private final JpaBeanFactory jpaBeanFactory;

    private final TransactionOperations transactionTemplate;

    private final CasConfigurationProperties casProperties;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        transactionTemplate.executeWithoutResult(Unchecked.consumer(status -> {
            val ticketEntity = getTicketEntityFrom(ticket);
            if (ticket instanceof TicketGrantingTicketAwareTicket
                && TicketGrantingTicketAwareTicket.class.cast(ticket).getTicketGrantingTicket() != null) {
                val parentId = encodeTicketId(((TicketGrantingTicketAwareTicket) ticket).getTicketGrantingTicket().getId());
                ticketEntity.setParentId(parentId);
            }
            this.entityManager.persist(ticketEntity);
            LOGGER.debug("Added ticket [{}] to registry.", ticketEntity.getId());
        }));
    }

    protected BaseTicketEntity getTicketEntityFrom(final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> {
            val encodeTicket = encodeTicket(ticket);
            return getJpaTicketEntityFactory().fromTicket(encodeTicket)
                .setPrincipalId(encodeTicketId(getPrincipalIdFrom(ticket)));
        });
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return transactionTemplate.execute(callback -> {
            try {
                val encTicketId = encodeTicketId(ticketId);
                if (StringUtils.isBlank(encTicketId)) {
                    return null;
                }
                val factory = getJpaTicketEntityFactory();
                val sql = String.format("SELECT t FROM %s t WHERE t.id = :id", factory.getEntityName());
                val query = entityManager.createQuery(sql, factory.getType());
                query.setParameter("id", encTicketId);
                query.setLockMode(this.lockType);
                val ticket = query.getSingleResult();
                val entity = getJpaTicketEntityFactory().toTicket(ticket);
                val result = decodeTicket(entity);
                return predicate.test(result) ? result : null;
            } catch (final NoResultException e) {
                LOGGER.debug("No record could be found for ticket [{}]", ticketId);
            }
            return null;
        });
    }

    @Override
    public int deleteTicket(final String ticketId) throws Exception {
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
            query.setLockMode(this.lockType);

            return query
                .getResultStream()
                .map(factory::toTicket)
                .map(this::decodeTicket)
                .collect(Collectors.toList());
        });
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
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
            .setParameter("principalId", encodeTicketId(principalId))
            .setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(factory::toTicket)
            .map(this::decodeTicket);
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
            val encTicketId = encodeTicketId(ticketIdToDelete);
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
