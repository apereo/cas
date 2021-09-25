package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.JpaYubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * This is {@link JpaYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerYubiKey")
@Slf4j
public class JpaYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {

    private static final String SELECT_QUERY = "SELECT r from " + JpaYubiKeyAccount.class.getSimpleName() + " r ";

    private static final String SELECT_ACCOUNT_QUERY = SELECT_QUERY.concat(" WHERE r.username = :username");

    @PersistenceContext(unitName = "yubiKeyEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return this.entityManager.createQuery(SELECT_QUERY, JpaYubiKeyAccount.class).getResultList();
    }

    @Override
    public YubiKeyAccount getAccountInternal(final String uid) {
        try {
            val account = fetchSingleYubiKeyAccount(uid);
            entityManager.detach(account);
            return account;
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found", e);
        }
        return null;
    }

    @Override
    public void delete(final String uid) {
        val account = fetchSingleYubiKeyAccount(uid);
        entityManager.remove(account);
        LOGGER.debug("Deleted [{}] record(s)", account);
    }

    @Override
    public void delete(final String username, final long deviceId) {
        try {
            val account = fetchSingleYubiKeyAccount(username);
            if (account != null && account.getDevices().removeIf(d -> deviceId == d.getId())) {
                entityManager.merge(account);
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found", e);
        }
    }

    @Override
    public void deleteAll() {
        this.entityManager.createQuery(SELECT_QUERY).getResultList().forEach(r -> entityManager.remove(r));
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request, final YubiKeyRegisteredDevice... device) {
        val jpaAccount = JpaYubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(CollectionUtils.wrapList(device))
            .build();
        return this.entityManager.merge(jpaAccount);
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount account) {
        val jpaAccount = JpaYubiKeyAccount.builder()
            .username(account.getUsername())
            .devices(account.getDevices())
            .build();
        return this.entityManager.merge(jpaAccount);
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        return this.entityManager.merge(account) != null;
    }

    private JpaYubiKeyAccount fetchSingleYubiKeyAccount(final String username) {
        return this.entityManager.createQuery(SELECT_ACCOUNT_QUERY, JpaYubiKeyAccount.class)
            .setParameter("username", username)
            .getSingleResult();
    }
}
