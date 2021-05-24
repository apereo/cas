package org.apereo.cas.ticket.registry.support;

import org.apereo.cas.ticket.registry.generic.JpaLockEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * JPA 2.0 implementation of an exclusive, non re-entrant lock.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class JpaLockingStrategy implements LockingStrategy {
    /**
     * Application identifier that identifies rows in the locking table,
     * each one of which may be for a different application or usage within
     * a single application.
     */
    private final String applicationId;

    /**
     * Unique identifier that identifies the client using this lock instance.
     */
    private final String uniqueId;

    /**
     * Amount of time in seconds lock may be held.
     */
    private final long lockTimeout;

    /**
     * Transactional entity manager from Spring context.
     */
    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    /**
     * Acquire the lock object.
     *
     * @param lock the lock
     * @return true, if successful
     */
    public boolean acquire(final JpaLockEntity lock) {
        lock.setUniqueId(this.uniqueId);
        if (this.lockTimeout > 0) {
            lock.setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(this.lockTimeout));
        } else {
            lock.setExpirationDate(null);
        }
        try {
            if (lock.getApplicationId() != null) {
                this.entityManager.merge(lock);
            } else {
                lock.setApplicationId(this.applicationId);
                this.entityManager.persist(lock);
            }
            return true;
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId, e);
            } else {
                LOGGER.info("[{}] could not obtain [{}] lock.", this.uniqueId, this.applicationId);
            }
        }
        return false;
    }

    @Override
    public boolean acquire() {
        try {
            val lock = this.entityManager.find(JpaLockEntity.class, this.applicationId, LockModeType.OPTIMISTIC);
            var result = false;
            if (lock != null) {
                val expDate = lock.getExpirationDate();
                if (lock.getUniqueId() == null) {
                    LOGGER.debug("[{}] trying to acquire [{}] lock.", this.uniqueId, this.applicationId);
                    result = acquire(lock);
                } else if (expDate == null || ZonedDateTime.now(ZoneOffset.UTC).isAfter(expDate)) {
                    LOGGER.debug("[{}] trying to acquire expired [{}] lock.", this.uniqueId, this.applicationId);
                    result = acquire(lock);
                }
            } else {
                LOGGER.debug("Creating [{}] lock initially held by [{}].", applicationId, uniqueId);
                result = acquire(new JpaLockEntity());
            }
            return result;
        } catch (final Exception e) {
            LOGGER.debug("[{}] failed querying for [{}] lock.", this.uniqueId, this.applicationId, e);
            return false;
        }
    }

    @Override
    public void release() {
        val lock = this.entityManager.find(JpaLockEntity.class, this.applicationId, LockModeType.OPTIMISTIC);
        if (lock == null) {
            return;
        }
        val owner = lock.getUniqueId();
        if (!this.uniqueId.equals(owner)) {
            throw new IllegalStateException("Cannot release lock owned by " + owner);
        }
        lock.setUniqueId(null);
        lock.setExpirationDate(null);
        LOGGER.debug("Releasing [{}] lock held by [{}].", this.applicationId, this.uniqueId);
        this.entityManager.persist(lock);
    }

}
