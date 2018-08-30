package org.apereo.cas.ticket.registry.support;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.ToString;
import lombok.Getter;

/**
 * JPA 2.0 implementation of an exclusive, non-reentrant lock.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@ToString
@Getter
public class JpaLockingStrategy implements LockingStrategy {

    /** Transactional entity manager from Spring context. */
    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    protected EntityManager entityManager;

    /**
     * Application identifier that identifies rows in the locking table,
     * each one of which may be for a different application or usage within
     * a single application.
     */
    private final String applicationId;

    /** Unique identifier that identifies the client using this lock instance. */
    private final String uniqueId;

    /** Amount of time in seconds lock may be held. */
    private final long lockTimeout;

    /**
     *
     * @param applicationId Application identifier that identifies a row in the lock
     *             table for which multiple clients vie to hold the lock.
     *             This must be the same for all clients contending for a
     *             particular lock.
     * @param uniqueId Identifier used to identify this instance in a row of the
     *             lock table.  Must be unique across all clients vying for
     *             locks for a given application ID.
     * @param lockTimeout Maximum amount of time in seconds lock may be held.
     *                  A value of zero indicates that locks are held indefinitely.
     *                  Use of a reasonable timeout facilitates recovery from node failures,
     *                  so setting to zero is discouraged.
     */
    public JpaLockingStrategy(final String applicationId, final String uniqueId, final long lockTimeout) {
        this.applicationId = applicationId;
        this.uniqueId = uniqueId;
        if (lockTimeout < 0) {
            throw new IllegalArgumentException("Lock timeout must be non-negative.");
        }
        this.lockTimeout = lockTimeout;
    }

    @Override
    public void release() {
        final Lock lock = this.entityManager.find(Lock.class, this.applicationId, LockModeType.OPTIMISTIC);
        if (lock == null) {
            return;
        }
        // Only the current owner can release the lock
        final String owner = lock.getUniqueId();
        if (!this.uniqueId.equals(owner)) {
            throw new IllegalStateException("Cannot release lock owned by " + owner);
        }
        lock.setUniqueId(null);
        lock.setExpirationDate(null);
        LOGGER.debug("Releasing [{}] lock held by [{}].", this.applicationId, this.uniqueId);
        this.entityManager.persist(lock);
    }

    /**
     * Acquire the lock object.
     *
     * @param lock the lock
     * @return true, if successful
     */
    public boolean acquire(final Lock lock) {
        lock.setUniqueId(this.uniqueId);
        if (this.lockTimeout > 0) {
            lock.setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(this.lockTimeout));
        } else {
            lock.setExpirationDate(null);
        }
        boolean success;
        try {
            if (lock.getApplicationId() != null) {
                this.entityManager.merge(lock);
            } else {
                lock.setApplicationId(this.applicationId);
                this.entityManager.persist(lock);
            }
            success = true;
        } catch (final Exception e) {
            success = false;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId, e);
            } else {
                LOGGER.info("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId);
            }
        }
        return success;
    }

    @Override
    public boolean acquire() {
        final Lock lock;
        try {
            lock = this.entityManager.find(Lock.class, this.applicationId, LockModeType.OPTIMISTIC);
        } catch (final Exception e) {
            LOGGER.debug("[{}] failed querying for [{}] lock.", this.uniqueId, this.applicationId, e);
            return false;
        }
        boolean result = false;
        if (lock != null) {
            final ZonedDateTime expDate = lock.getExpirationDate();
            if (lock.getUniqueId() == null) {
                // No one currently possesses lock
                LOGGER.debug("[{}] trying to acquire [{}] lock.", this.uniqueId, this.applicationId);
                result = acquire(lock);
            } else if (expDate == null || ZonedDateTime.now(ZoneOffset.UTC).isAfter(expDate)) {
                // Acquire expired lock regardless of who formerly owned it
                LOGGER.debug("[{}] trying to acquire expired [{}] lock.", this.uniqueId, this.applicationId);
                result = acquire(lock);
            }
        } else {
            // First acquisition attempt for this applicationId
            LOGGER.debug("Creating [{}] lock initially held by [{}].", applicationId, uniqueId);
            result = acquire(new Lock());
        }
        return result;
    }

    /**
     * Describes a database lock.
     *
     * @author Marvin S. Addison
     *
     */
    @Entity
    @Table(name = "locks")
    @Getter
    @Setter
    public static class Lock implements Serializable {

        private static final long serialVersionUID = -5750740484289616656L;

        /** column name that holds application identifier. */
        @org.springframework.data.annotation.Id
        @Id
        @Column(name = "application_id")
        private String applicationId;

        /** Database column name that holds unique identifier. */
        @Column(name = "unique_id")
        private String uniqueId;

        /** Database column name that holds expiration date. */
        @Column(name = "expiration_date")
        private ZonedDateTime expirationDate;

        @Version
        @Column(name = "lockVer", columnDefinition = "integer DEFAULT 0", nullable = false)
        private final Long version = 0L;
    }
}
