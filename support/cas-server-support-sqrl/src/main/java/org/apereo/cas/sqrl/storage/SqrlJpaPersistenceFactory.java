package org.apereo.cas.sqrl.storage;

import com.github.dbadia.sqrl.server.SqrlAuthenticationStatus;
import com.github.dbadia.sqrl.server.SqrlFlag;
import com.github.dbadia.sqrl.server.SqrlPersistence;
import com.github.dbadia.sqrl.server.data.SqrlAutoCloseablePersistence;
import com.github.dbadia.sqrl.server.data.SqrlCorrelator;
import com.github.dbadia.sqrl.server.data.SqrlIdentity;
import com.github.dbadia.sqrl.server.data.SqrlUsedNutToken;
import com.github.dbadia.sqrl.server.exception.SqrlDebugException;
import com.github.dbadia.sqrl.server.exception.SqrlPersistenceException;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SqrlJpaPersistenceFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlJpaPersistenceFactory extends com.github.dbadia.sqrl.server.data.SqrlJpaPersistenceFactory {

    public SqrlJpaPersistenceFactory() {
    }

    @Override
    public SqrlPersistence createSqrlPersistence() {
        return new SqrlAutoCloseablePersistence(new JpaSqrlPersistence());
    }

    public static class JpaSqrlPersistence implements SqrlPersistence {
        private static final Logger LOGGER = LoggerFactory.getLogger(JpaSqrlPersistence.class);

        private static final String PARAM_CORRELATOR = "correlator";

        private static final Map<EntityManager, Long> LAST_USED_TIME_TABLE = new WeakHashMap<>();
        private static final Map<EntityManager, Exception> CREATED_BY_STACK_TABLE = new ConcurrentHashMap<>();

        private final EntityManager entityManager;

        public JpaSqrlPersistence() {
            final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            final EntityManagerFactory mgr = ctx.getBean("sqrlEntityManagerFactory", EntityManagerFactory.class);
            this.entityManager = mgr.createEntityManager();
            this.entityManager.getTransaction().begin();
        }

        private static void updateLastUsed(final EntityManager entityManger) {
            LAST_USED_TIME_TABLE.put(entityManger, System.currentTimeMillis());
        }

        @Override
        public boolean doesSqrlIdentityExistByIdk(final String sqrlIdk) {
            updateLastUsed(entityManager);
            return fetchSqrlIdentity(sqrlIdk) != null;
        }

        @Override
        public void updateIdkForSqrlIdentity(final String previousSqrlIdk, final String newSqrlIdk) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = fetchRequiredSqrlIdentity(previousSqrlIdk);
            sqrlIdentity.setIdk(newSqrlIdk);
        }

        private SqrlIdentity fetchSqrlIdentity(final String sqrlIdk) {
            updateLastUsed(entityManager);
            return (SqrlIdentity) returnOneOrNull(
                    entityManager.createQuery("SELECT i FROM SqrlIdentity i WHERE i.idk = :sqrlIdk")
                            .setParameter("sqrlIdk", sqrlIdk).getResultList());
        }

        private SqrlIdentity fetchRequiredSqrlIdentity(final String sqrlIdk) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = fetchSqrlIdentity(sqrlIdk);
            if (sqrlIdentity == null) {
                throw new SqrlPersistenceException("SqrlIdentity does not exist for idk=" + sqrlIdk);
            } else {
                return sqrlIdentity;
            }
        }

        @Override
        public SqrlIdentity fetchSqrlIdentityByUserXref(final String userXref) {
            updateLastUsed(entityManager);
            return (SqrlIdentity) returnOneOrNull(
                    entityManager.createQuery("SELECT i FROM SqrlIdentity i WHERE i.nativeUserXref = :userXref")
                            .setParameter("userXref", userXref).getResultList());
        }

        @Override
        public void deleteSqrlIdentity(final String sqrlIdk) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = fetchSqrlIdentity(sqrlIdk);
            if (sqrlIdentity == null) {
                LOGGER.warn("Can't find idk " + sqrlIdk + " to delete");
            } else {
                entityManager.remove(sqrlIdentity);
            }
        }

        @Override
        public void userAuthenticatedViaSqrl(final String sqrlIdk, final String correlatorString) {
            updateLastUsed(entityManager);
            // Find the sqrlIdentity and mark SQRL authentication as occurred
            final SqrlCorrelator sqrlCorrelator = fetchSqrlCorrelatorRequired(correlatorString);
            sqrlCorrelator.setAuthenticationStatus(SqrlAuthenticationStatus.AUTH_COMPLETE);
            final SqrlIdentity sqrlIdentity = fetchRequiredSqrlIdentity(sqrlIdk);
            sqrlCorrelator.setAuthenticatedIdentity(sqrlIdentity);
        }

        @Override
        public void updateNativeUserXref(final long sqrlIdentityDbId, final String nativeUserXref) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = entityManager.find(SqrlIdentity.class, sqrlIdentityDbId);
            sqrlIdentity.setNativeUserXref(nativeUserXref);
        }

    /* ************************ Sqrl Correlator methods *****************************/

        @Override
        public SqrlCorrelator fetchSqrlCorrelator(final String sqrlCorrelatorString) {
            updateLastUsed(entityManager);
            return (SqrlCorrelator) returnOneOrNull(
                    entityManager.createQuery("SELECT i FROM SqrlCorrelator i WHERE i.value = :correlator")
                            .setParameter(PARAM_CORRELATOR, sqrlCorrelatorString).getResultList());
        }

        @Override
        public SqrlCorrelator fetchSqrlCorrelatorRequired(final String sqrlCorrelatorString) {
            updateLastUsed(entityManager);
            final SqrlCorrelator sqrlCorrelator = fetchSqrlCorrelator(sqrlCorrelatorString);
            if (sqrlCorrelator == null) {
                throw new SqrlPersistenceException("SqrlCorrelator does not exist for correlator=" + sqrlCorrelatorString);
            } else {
                return sqrlCorrelator;
            }
        }

        @Override
        public Map<String, SqrlCorrelator> fetchSqrlCorrelatorsDetached(final Set<String> correlatorStringSet) {
            updateLastUsed(entityManager);
            if (correlatorStringSet.isEmpty()) {
                return Collections.emptyMap();
            }
            final StringBuilder buf = new StringBuilder("SELECT i FROM SqrlCorrelator i WHERE ");

            for (int i = 0; i < correlatorStringSet.size(); i++) {
                buf.append(" i.value = :correlator").append(i).append(" OR");
            }
            buf.replace(buf.length() - 3, buf.length(), ""); // Remove OR
            final TypedQuery<SqrlCorrelator> query = entityManager.createQuery(buf.toString(), SqrlCorrelator.class);
            int i = 0;
            for (final String correlatorString : correlatorStringSet) {
                query.setParameter(PARAM_CORRELATOR + (i++), correlatorString);
            }

            // Parse the result into a table
            final Map<String, SqrlCorrelator> resultTable = new ConcurrentHashMap<>();
            for (final SqrlCorrelator correlator : query.getResultList()) {
                entityManager.detach(correlator);
                resultTable.put(correlator.getCorrelatorString(), correlator);
            }
            return resultTable;
        }

        @Override
        public Map<String, SqrlAuthenticationStatus> fetchSqrlCorrelatorStatusUpdates(
                final Map<String, SqrlAuthenticationStatus> correlatorToCurrentStatusTable) {
            updateLastUsed(entityManager);
            if (correlatorToCurrentStatusTable.isEmpty()) {
                return Collections.emptyMap();
            }
            final StringBuilder buf = new StringBuilder("SELECT i FROM SqrlCorrelator i WHERE ");

            int counter = 0;
            for (final Map.Entry<String, SqrlAuthenticationStatus> entry : correlatorToCurrentStatusTable.entrySet()) {
                buf.append(" (i.value = :correlator").append(counter);
                // If current state is AUTH_COMPLETE, always return the result
                if (entry.getValue() != SqrlAuthenticationStatus.AUTH_COMPLETE) {
                    buf.append(" AND i.authenticationStatus <> :authenticationStatus").append(counter);
                }
                buf.append(" ) OR");
                counter++;
            }
            buf.replace(buf.length() - 2, buf.length(), ""); // Remove "OR"
            final TypedQuery<SqrlCorrelator> query = entityManager.createQuery(buf.toString(), SqrlCorrelator.class);
            counter = 0;
            final StringBuilder debugBuf = new StringBuilder(buf);
            for (final Map.Entry<String, SqrlAuthenticationStatus> entry : correlatorToCurrentStatusTable.entrySet()) {
                query.setParameter(PARAM_CORRELATOR + counter, entry.getKey());
                updateDebugBuf(debugBuf, ":correlator" + counter, entry.getKey());
                if (entry.getValue() != SqrlAuthenticationStatus.AUTH_COMPLETE) {
                    query.setParameter("authenticationStatus" + counter, entry.getValue());
                    updateDebugBuf(debugBuf, ":authenticationStatus" + counter, entry.getValue().toString());
                }
                counter++;
            }

            LOGGER.debug("monitor correaltor for change SQL: {}", debugBuf.toString());
            // Parse the result into a table
            final Map<String, SqrlAuthenticationStatus> resultTable = new ConcurrentHashMap<>();
            for (final SqrlCorrelator correlator : query.getResultList()) {
                resultTable.put(correlator.getCorrelatorString(), correlator.getAuthenticationStatus());
            }
            return resultTable;
        }

        private static void updateDebugBuf(final StringBuilder buf, final String name, final String value) {
            final int start = buf.indexOf(name);
            final int end = start + name.length();
            buf.replace(start, end, "'" + value + "'");
        }

        @Override
        public void storeSqrlDataForSqrlIdentity(final String sqrlIdk, final Map<String, String> dataToStore) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = fetchSqrlIdentity(sqrlIdk);
            if (sqrlIdentity == null) {
                throw new SqrlPersistenceException("SqrlIdentity not found for " + sqrlIdk);
            }
            storeSqrlDataForSqrlIdentity(sqrlIdentity, dataToStore);
        }

        private void storeSqrlDataForSqrlIdentity(final SqrlIdentity sqrlIdentity, final Map<String, String> dataToStore) {
            updateLastUsed(entityManager);
            // Update any SQRL specific data we have received from the SQRL client
            if (!dataToStore.isEmpty()) {
                sqrlIdentity.getIdentityDataTable().putAll(dataToStore);
            }
            entityManager.persist(sqrlIdentity);
        }

        @Override
        public String fetchSqrlIdentityDataItem(final String sqrlIdk, final String toFetch) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = fetchSqrlIdentity(sqrlIdk);
            if (sqrlIdentity == null) {
                throw new SqrlPersistenceException("Couldn't find SqrlIdentity for idk " + sqrlIdk);
            } else {
                return sqrlIdentity.getIdentityDataTable().get(toFetch);
            }
        }

        @Override
        public boolean hasTokenBeenUsed(final String nutTokenString) {
            updateLastUsed(entityManager);
            return entityManager.find(SqrlUsedNutToken.class, nutTokenString) != null;
        }

        @Override
        public void markTokenAsUsed(final String nutTokenString, final Date expiryTime) {
            updateLastUsed(entityManager);
            final SqrlUsedNutToken sqrlUsedNutToken = new SqrlUsedNutToken(nutTokenString, expiryTime);
            entityManager.persist(sqrlUsedNutToken);
        }

        @Override
        public String fetchTransientAuthData(final String correlator, final String dataName) {
            updateLastUsed(entityManager);
            final SqrlCorrelator correlatorObject = fetchSqrlCorrelatorRequired(correlator);
            return correlatorObject.getTransientAuthDataTable().get(dataName);
        }

        private Object returnOneOrNull(@SuppressWarnings("rawtypes") final List resultList) {
            if (resultList == null || resultList.isEmpty()) {
                return null;
            } else if (resultList.size() == 1) {
                return resultList.get(0);
            } else {
                throw new SqrlPersistenceException("Expected one, but found multiple results: " + resultList);
            }
        }

        @Override
        public void closeCommit() {
            closeServletTransaction(true);
        }

        @Override
        public void closeRollback() {
            closeServletTransaction(false);
        }

        private void closeServletTransaction(final boolean commit) {
            if (!entityManager.isOpen()) {
                throw new SqrlPersistenceException("EntityManager is not open");
            }
            if (commit) {
                entityManager.getTransaction().commit();
            } else {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }

        @Override
        public Boolean fetchSqrlFlagForIdentity(final String sqrlIdk, final SqrlFlag flagToFetch) {
            return fetchRequiredSqrlIdentity(sqrlIdk).getFlagTable().get(flagToFetch);
        }

        @Override
        public void setSqrlFlagForIdentity(final String sqrlIdk, final SqrlFlag flagToSet, final boolean valueToSet) {
            final SqrlIdentity sqrlIdentity = fetchRequiredSqrlIdentity(sqrlIdk);
            sqrlIdentity.getFlagTable().put(flagToSet, valueToSet);
            entityManager.persist(sqrlIdentity);
        }

        @Override
        public void createAndEnableSqrlIdentity(final String sqrlIdk, final Map<String, String> identityDataTable) {
            updateLastUsed(entityManager);
            final SqrlIdentity sqrlIdentity = new SqrlIdentity(sqrlIdk);
            sqrlIdentity.getFlagTable().put(SqrlFlag.SQRL_AUTH_ENABLED, true);
            sqrlIdentity.getIdentityDataTable().putAll(identityDataTable);
            entityManager.persist(sqrlIdentity);
        }

        @Override
        public SqrlCorrelator createCorrelator(final String correlatorString, final Date expiryTime) {
            final SqrlCorrelator sqrlCorrelator = new SqrlCorrelator(correlatorString, expiryTime);
            entityManager.persist(sqrlCorrelator);
            return sqrlCorrelator;
        }

        @Override
        public void deleteSqrlCorrelator(final SqrlCorrelator sqrlCorrelator) {
            if (sqrlCorrelator == null) {
                return;
            }
            SqrlCorrelator toRemove = sqrlCorrelator;
            updateLastUsed(entityManager);
            if (!entityManager.contains(sqrlCorrelator)) {
                toRemove = fetchSqrlCorrelator(sqrlCorrelator.getCorrelatorString());
            }
            if (toRemove == null) {
                LOGGER.debug("INFO-ONLY-STACK: Attempt to remove correlator that doesn't exist", new SqrlDebugException());
            } else {
                entityManager.remove(toRemove);
            }
        }

        @Override
        public boolean isClosed() {
            return !entityManager.isOpen();
        }

        @Override
        public void cleanUpExpiredEntries() {
            final Date now = new Date();
            int rowsDeleted = entityManager.createQuery("DELETE FROM SqrlCorrelator i WHERE i.expiryTime < :now")
                    .setParameter("now", now, TemporalType.TIMESTAMP).executeUpdate();
            if (rowsDeleted > 0) {
                LOGGER.info("SqrlCorrelatorc cleanup deleted {} rows", rowsDeleted);
            }

            rowsDeleted = entityManager.createQuery("DELETE FROM SqrlUsedNutToken i WHERE i.expiryTime < :now")
                    .setParameter("now", now, TemporalType.TIMESTAMP).executeUpdate();
            if (rowsDeleted > 0) {
                LOGGER.info("SqrlUsedNutToken cleanup deleted {} rows", rowsDeleted);
            }
        }

        /**
         * A task which periodically checks the state of various {@link EntityManager} instances to ensure they are being
         * closed properly by the library
         *
         * @author Dave Badia
         */
        public static final class SqrlJpaEntityManagerMonitorTask implements Runnable {
            private static final long ENTITY_MANAGER_IDLE_WARN_THRESHOLD_MINUTES = 5;
            private static final long ENTITY_MANAGER_IDLE_WARN_THRESHOLD_MS = TimeUnit.MINUTES
                    .toMillis(ENTITY_MANAGER_IDLE_WARN_THRESHOLD_MINUTES);

            public SqrlJpaEntityManagerMonitorTask() {
                // As required by
            }

            @Override
            public void run() {
                try {
                    LOGGER.debug("Running EntityManagerMonitorTimerTask");
                    final Iterator<EntityManager> iter = CREATED_BY_STACK_TABLE.keySet().iterator();
                    while (iter.hasNext()) {
                        final EntityManager entityManager = iter.next();
                        if (!entityManager.isOpen()) {
                            LOGGER.debug("entityManager closed, removing from monitor table");
                            iter.remove();
                            LAST_USED_TIME_TABLE.remove(entityManager); // May or may not exist, ok
                        } else {
                            final Long lastUsed = LAST_USED_TIME_TABLE.get(entityManager);
                            if (lastUsed == null) {
                                LOGGER.error(
                                        "EntityManagerMonitorTask found null lastUsedTime for entityManager which was created at",
                                        CREATED_BY_STACK_TABLE.get(entityManager));
                            } else {
                                if (System.currentTimeMillis() - lastUsed > ENTITY_MANAGER_IDLE_WARN_THRESHOLD_MS) {
                                    LOGGER.error("Entity Manager is still open and has not been used for "
                                                    + ENTITY_MANAGER_IDLE_WARN_THRESHOLD_MINUTES + " minutes.  Was created from",
                                            CREATED_BY_STACK_TABLE.get(entityManager));
                                }
                            }
                        }
                    }
                } catch (final RuntimeException e) {
                    LOGGER.error("Error running entity manager monitor check", e);
                }
            }
        }
    }
}
