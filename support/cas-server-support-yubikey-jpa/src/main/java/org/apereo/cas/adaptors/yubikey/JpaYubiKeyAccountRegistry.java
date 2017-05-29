package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.dao.JpaYubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * This is {@link JpaYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerYubiKey")
public class JpaYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaYubiKeyAccountRegistry.class);

    private static final String SELECT_QUERY = "SELECT r from YubiKeyAccount r ";

    @PersistenceContext(unitName = "yubiKeyEntityManagerFactory")
    private EntityManager entityManager;

    public JpaYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        try {
            return this.entityManager.createQuery(SELECT_QUERY.concat("where r.username = :username"),
                    JpaYubiKeyAccountRegistry.class)
                    .setParameter("username", uid)
                    .getSingleResult() != null;
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found for id [{}]", uid);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean registerAccount(final String uid, final String yubikeyPublicId) {
        final JpaYubiKeyAccount account = new JpaYubiKeyAccount();
        account.setPublicId(yubikeyPublicId);
        account.setUsername(uid);
        return this.entityManager.merge(account) != null;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        try {
            return this.entityManager.createQuery(SELECT_QUERY.concat("where r.username = :username and r.publicId = :publicId"),
                    JpaYubiKeyAccountRegistry.class)
                    .setParameter("username", uid)
                    .setParameter("publicId", yubikeyPublicId)
                    .getSingleResult() != null;
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found for id [{}] and public id [{}]", uid, yubikeyPublicId);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return false;
    }
}
