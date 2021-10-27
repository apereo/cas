/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA 2.0 implementation of an exclusive, non-reintrant lock.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 *
 */
public class JpaLockingStrategy implements LockingStrategy {
    
    /** Default lock timeout is 1 hour. */
    public static final int DEFAULT_LOCK_TIMEOUT = 3600;

    /** Transactional entity manager from Spring context. */
    @NotNull
    @PersistenceContext
    protected EntityManager entityManager;

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Application identifier that identifies rows in the locking table,
     * each one of which may be for a different application or usage within
     * a single application.
     */
    @NotNull
    private String applicationId;

    /** Unique identifier that identifies the client using this lock instance */
    @NotNull
    private String uniqueId;

    /** Amount of time in seconds lock may be held */
    private int lockTimeout = DEFAULT_LOCK_TIMEOUT;


    /**
     * @param  id  Application identifier that identifies a row in the lock
     *             table for which multiple clients vie to hold the lock.
     *             This must be the same for all clients contending for a
     *             particular lock.
     */
    public void setApplicationId(final String id) {
        this.applicationId = id;
    }


    /**
     * @param  id  Identifier used to identify this instance in a row of the
     *             lock table.  Must be unique across all clients vying for
     *             locks for a given application ID.
     */
    public void setUniqueId(final String id) {
        this.uniqueId = id;
    }


    /**
     * @param  seconds  Maximum amount of time in seconds lock may be held.
     *                  A value of zero indicates that locks are held indefinitely.
     *                  Use of a reasonable timeout facilitates recovery from node failures,
     *                  so setting to zero is discouraged.
     */
    public void setLockTimeout(final int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Lock timeout must be non-negative.");
        }
        this.lockTimeout = seconds;
    }
    

    /** {@inheritDoc} */
    @Transactional(readOnly = false)
    public boolean acquire() {
        Lock lock;
        try {
            lock = entityManager.find(Lock.class, applicationId, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            logger.debug("{} failed querying for {} lock.", new Object[] {uniqueId, applicationId, e});
            return false;
        }
        
        boolean result = false;
        if (lock != null) {
	        final Date expDate = lock.getExpirationDate();
	        if (lock.getUniqueId() == null) {
	            // No one currently possesses lock
	            logger.debug("{} trying to acquire {} lock.", uniqueId, applicationId);
	            result = acquire(entityManager, lock);
	        } else if (expDate != null && new Date().after(expDate)) {
	            // Acquire expired lock regardless of who formerly owned it
	            logger.debug("{} trying to acquire expired {} lock.", uniqueId, applicationId);
	            result = acquire(entityManager, lock);
	        }
        } else {
            // First acquisition attempt for this applicationId
            logger.debug("Creating {} lock initially held by {}.", applicationId, uniqueId);
            result = acquire(entityManager, new Lock());
        }
        return result;
    }


    /** {@inheritDoc} */
    @Transactional(readOnly = false)
    public void release() {
        final Lock lock = entityManager.find(Lock.class, applicationId, LockModeType.PESSIMISTIC_WRITE);
      
        if (lock == null) {
            return;
        }
        // Only the current owner can release the lock
        final String owner = lock.getUniqueId();
        if (uniqueId.equals(owner)) {
            lock.setUniqueId(null);
            lock.setExpirationDate(null);
            logger.debug("Releasing {} lock held by {}.", applicationId, uniqueId);
            entityManager.persist(lock);
        } else {
            throw new IllegalStateException("Cannot release lock owned by " + owner);
        }
    }
    
   
    /**
     * Gets the current owner of the lock as determined by querying for
     * uniqueId.
     *
     * @return  Current lock owner or null if no one presently owns lock.
     */
    @Transactional(readOnly = true)
    public String getOwner() {
        final Lock lock = entityManager.find(Lock.class, applicationId);
        if (lock != null) {
            return lock.getUniqueId();
        }
        return null;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return uniqueId;
    }


    private boolean acquire(final EntityManager em, Lock lock) {
        lock.setUniqueId(uniqueId);
        if (lockTimeout > 0) {
	        final Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.SECOND, lockTimeout);
	        lock.setExpirationDate(cal.getTime());
        } else {
            lock.setExpirationDate(null);
        }
        boolean success = false;
        try {
            if (lock.getApplicationId() != null) {
                lock = em.merge(lock);
            } else {
                lock.setApplicationId(applicationId);
                em.persist(lock);
            }
            success = true;
        } catch (PersistenceException e) {
            success = false;
            if (logger.isDebugEnabled()) {
                logger.debug("{} could not obtain {} lock.", new Object[] {uniqueId, applicationId, e});
            } else {
                logger.info("{} could not obtain {} lock.", uniqueId, applicationId);
            }
        }
        return success; 
    }


    /**
     * Describes a database lock.
     *
     * @author Marvin S. Addison
     * @version $Revision: $
     *
     */
    @Entity
    @Table(name = "locks")
    public static class Lock {
        /** column name that holds application identifier */
        @Id
        @Column(name="application_id")
        private String applicationId;
        
        /** Database column name that holds unique identifier */
        @Column(name="unique_id")
        private String uniqueId;

        /** Database column name that holds expiration date */
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name="expiration_date")
        private Date expirationDate;


        /**
         * @return the applicationId
         */
        public String getApplicationId() {
            return applicationId;
        }

        /**
         * @param applicationId the applicationId to set
         */
        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        /**
         * @return the uniqueId
         */
        public String getUniqueId() {
            return uniqueId;
        }

        /**
         * @param uniqueId the uniqueId to set
         */
        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        /**
         * @return the expirationDate
         */
        public Date getExpirationDate() {
            return expirationDate;
        }

        /**
         * @param expirationDate the expirationDate to set
         */
        public void setExpirationDate(Date expirationDate) {
            this.expirationDate = expirationDate;
        }
    }
}
