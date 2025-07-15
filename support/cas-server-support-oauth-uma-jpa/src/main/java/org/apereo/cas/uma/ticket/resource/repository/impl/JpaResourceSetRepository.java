package org.apereo.cas.uma.ticket.resource.repository.impl;

import org.apereo.cas.uma.ticket.resource.JpaResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.BaseResourceSetRepository;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link JpaResourceSetRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "umaTransactionManager")
@ToString
public class JpaResourceSetRepository extends BaseResourceSetRepository {
    private static final String ENTITY_NAME = JpaResourceSet.class.getSimpleName();

    @PersistenceContext(unitName = "umaResourceJpaContext")
    private EntityManager entityManager;

    @Override
    public ResourceSet save(final ResourceSet set) {
        if (!validateResourceSetScopes(set)) {
            throw new IllegalArgumentException("Cannot save a resource set with inconsistent scopes.");
        }
        val jpaResource = new JpaResourceSet();
        FunctionUtils.doUnchecked(__ -> BeanUtils.copyProperties(jpaResource, set));
        return entityManager.merge(jpaResource);
    }

    @Override
    public Collection<? extends ResourceSet> getAll() {
        val query = String.format("SELECT r FROM %s r", ENTITY_NAME);
        return entityManager.createQuery(query, JpaResourceSet.class).getResultList();
    }

    @Override
    public Optional<ResourceSet> getById(final long id) {
        try {
            val query = String.format("SELECT r FROM %s r WHERE r.id = :id", ENTITY_NAME);
            val resourceSet = entityManager.createQuery(query, JpaResourceSet.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(resourceSet);
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void remove(final ResourceSet set) {
        if (entityManager.contains(set)) {
            entityManager.remove(set);
        } else {
            entityManager.remove(entityManager.merge(set));
        }
    }

    @Override
    public void removeAll() {
        val query = String.format("DELETE FROM %s", ENTITY_NAME);
        entityManager.createQuery(query).executeUpdate();
    }
}
