package org.apereo.cas.uma.ticket.resource.repository.impl;

import org.apereo.cas.uma.ticket.resource.JpaResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.BaseResourceSetRepository;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link JpaResourceSetRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "umaTransactionManager")
@ToString
public class JpaResourceSetRepository extends BaseResourceSetRepository {
    private static final String ENTITY_NAME = JpaResourceSet.class.getSimpleName();

    @PersistenceContext(unitName = "umaEntityManagerFactory")
    private transient EntityManager entityManager;

    @SneakyThrows
    @Override
    public ResourceSet save(final ResourceSet set) {
        if (!validateResourceSetScopes(set)) {
            throw new IllegalArgumentException("Cannot save a resource set with inconsistent scopes.");
        }
        val jpaResource = new JpaResourceSet();
        BeanUtils.copyProperties(jpaResource, set);

        val isNew = jpaResource.getId() <= 0;
        val r = this.entityManager.merge(jpaResource);
        if (!isNew) {
            this.entityManager.persist(r);
        }
        return r;
    }

    @Override
    public Collection<? extends ResourceSet> getAll() {
        val query = String.format("SELECT r FROM %s r", ENTITY_NAME);
        return this.entityManager.createQuery(query, JpaResourceSet.class).getResultList();
    }

    @Override
    public Optional<ResourceSet> getById(final long id) {
        try {
            val query = String.format("SELECT r FROM %s r WHERE r.id = :id", ENTITY_NAME);
            val r = this.entityManager.createQuery(query, JpaResourceSet.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(r);
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void remove(final ResourceSet set) {
        if (this.entityManager.contains(set)) {
            this.entityManager.remove(set);
        } else {
            this.entityManager.remove(this.entityManager.merge(set));
        }
    }

    @Override
    public void removeAll() {
        val query = String.format("DELETE FROM %s", ENTITY_NAME);
        this.entityManager.createQuery(query).executeUpdate();
    }
}
