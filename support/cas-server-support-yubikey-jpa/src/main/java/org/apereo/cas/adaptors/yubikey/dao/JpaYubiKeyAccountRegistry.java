package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.JpaYubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link JpaYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
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
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(token));

            val results = this.entityManager.createQuery(SELECT_ACCOUNT_QUERY, JpaYubiKeyAccount.class)
                .setParameter("username", uid)
                .setMaxResults(1)
                .getResultList();

            val account = results.isEmpty() ? new JpaYubiKeyAccount() : results.get(0);
            account.registerDevice(getCipherExecutor().encode(yubikeyPublicId));
            account.setUsername(uid);
            return this.entityManager.merge(account) != null;
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        try {
            return this.entityManager.createQuery(SELECT_QUERY, JpaYubiKeyAccount.class)
                .getResultList()
                .stream()
                .peek(it -> {
                    val devices = it.getDeviceIdentifiers().stream()
                        .map(pubId -> getCipherExecutor().decode(pubId))
                        .collect(Collectors.toCollection(ArrayList::new));
                    it.setDeviceIdentifiers(devices);
                })
                .collect(Collectors.toList());
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found");
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        try {
            val account = this.entityManager.createQuery(SELECT_ACCOUNT_QUERY, JpaYubiKeyAccount.class)
                .setParameter("username", uid)
                .getSingleResult();
            val devices = account.getDeviceIdentifiers().stream()
                .map(pubId -> getCipherExecutor().decode(pubId))
                .collect(Collectors.toCollection(ArrayList::new));
            val yubiKeyAccount = new JpaYubiKeyAccount(account.getId(), devices, account.getUsername());
            return Optional.of(yubiKeyAccount);
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found", e);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String uid) {
        val count = this.entityManager.createQuery("DELETE FROM " + JpaYubiKeyAccount.class.getSimpleName() + " r WHERE r.username = :username")
            .setParameter("username", uid)
            .executeUpdate();
        LOGGER.debug("Deleted [{}] record(s)", count);
    }

    @Override
    public void deleteAll() {
        this.entityManager.createQuery("DELETE FROM " + JpaYubiKeyAccount.class.getSimpleName()).executeUpdate();
    }
}
