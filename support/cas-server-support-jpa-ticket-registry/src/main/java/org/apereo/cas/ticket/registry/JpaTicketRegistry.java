package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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
@EnableTransactionManagement
@Transactional(transactionManager = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@Slf4j
@RequiredArgsConstructor
@Getter
public class JpaTicketRegistry extends AbstractTicketRegistry {
    /**
     * Bean name of the transaction manager.
     */
    public static final String BEAN_NAME_TRANSACTION_MANAGER = "ticketTransactionManager";

    private final LockModeType lockType;

    private final TicketCatalog ticketCatalog;

    private final JpaBeanFactory jpaBeanFactory;

    private final TransactionTemplate transactionTemplate;

    private final CasConfigurationProperties casProperties;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        this.transactionTemplate.executeWithoutResult(status -> {
            val encodeTicket = encodeTicket(ticket);
            val factory = getJpaTicketEntityFactory();
            val ticketEntity = factory.fromTicket(encodeTicket);
            if (ticket.getTicketGrantingTicket() != null) {
                ticketEntity.setParentId(encodeTicketId(ticket.getTicketGrantingTicket().getId()));
            }
            this.entityManager.persist(ticketEntity);
            LOGGER.debug("Added ticket [{}] to registry.", encodeTicket);
        });
    }

    @Override
    @Transactional(transactionManager = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
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
            if (predicate.test(result)) {
                return result;
            }
            return null;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for ticket [{}]", ticketId);
        }
        return null;
    }

    @Override
    public int deleteTicket(final String ticketId) {
        return super.deleteTicket(ticketId);
    }

    @Override
    public long deleteAll() {
        val factory = getJpaTicketEntityFactory();
        val query = entityManager.createQuery(String.format("DELETE FROM %s", factory.getEntityName()));
        return query.executeUpdate();
    }

    @Override
    @Transactional(transactionManager = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public Collection<? extends Ticket> getTickets() {
        val factory = getJpaTicketEntityFactory();
        val sql = String.format("SELECT t FROM %s t", factory.getEntityName());
        val query = entityManager.createQuery(sql, factory.getType());
        query.setLockMode(this.lockType);

        return query
            .getResultStream()
            .map(factory::toTicket)
            .map(this::decodeTicket)
            .collect(Collectors.toList());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.trace("Updating ticket [{}]", ticket);
        val encodeTicket = this.encodeTicket(ticket);

        val factory = getJpaTicketEntityFactory();
        val ticketEntity = factory.fromTicket(encodeTicket);

        this.entityManager.merge(ticketEntity);
        LOGGER.debug("Updated ticket [{}]", encodeTicket);
        return encodeTicket;
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
    @Transactional(transactionManager = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public long sessionCount() {
        val factory = getJpaTicketEntityFactory();
        val md = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", factory.getEntityName());
        val query = this.entityManager.createQuery(sql).setParameter("type", md.getImplementationClass().getName());
        return countToLong(query.getSingleResult());
    }

    @Override
    @Transactional(transactionManager = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public long serviceTicketCount() {
        val factory = getJpaTicketEntityFactory();
        val md = this.ticketCatalog.find(ServiceTicket.PREFIX);
        val sql = String.format("SELECT COUNT(t.id) FROM %s t WHERE t.type=:type", factory.getEntityName());
        val query = this.entityManager.createQuery(sql).setParameter("type", md.getImplementationClass().getName());
        return countToLong(query.getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val factory = getJpaTicketEntityFactory();
        val result = this.transactionTemplate.execute(transactionStatus -> {
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
            return totalCount != 0;
        });
        return Objects.requireNonNull(result);
    }

    private JpaTicketEntityFactory getJpaTicketEntityFactory() {
        val jpa = casProperties.getTicket().getRegistry().getJpa();
        return new JpaTicketEntityFactory(jpa.getDialect());
    }

    /**
     * Delete ticket granting tickets.
     *
     * @param ticketId the ticket id
     * @return the total count
     */
    private int deleteTicketGrantingTickets(final String ticketId) {
        val factory = getJpaTicketEntityFactory();
        var sql = String.format("DELETE FROM %s t WHERE t.parentId = :id OR t.id = :id", factory.getEntityName());
        LOGGER.trace("Creating delete query [{}] for ticket id [{}]", sql, ticketId);
        var query = entityManager.createQuery(sql);
        query.setParameter("id", ticketId);
        return query.executeUpdate();
    }
}
