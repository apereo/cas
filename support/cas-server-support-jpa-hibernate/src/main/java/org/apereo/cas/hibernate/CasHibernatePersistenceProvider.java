package org.apereo.cas.hibernate;

import org.apereo.cas.jpa.JpaPersistenceProviderContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasHibernatePersistenceProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasHibernatePersistenceProvider extends HibernatePersistenceProvider {
    private final JpaPersistenceProviderContext providerContext;

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo info, final Map properties) {
        val filtered = CollectionUtils.intersection(info.getManagedClassNames(), providerContext.getIncludeEntityClasses());
        LOGGER.trace("Filtered entity classes for entity manager are [{}]", filtered);
        val mergedClassesAndPackages = new HashSet<String>(filtered);
        if (info instanceof SmartPersistenceUnitInfo) {
            mergedClassesAndPackages.addAll(((SmartPersistenceUnitInfo) info).getManagedPackages());
        }
        val persistenceUnit = new CasPersistenceUnitInfoDescriptor(info, new ArrayList<>(mergedClassesAndPackages));
        return new EntityManagerFactoryBuilderImpl(persistenceUnit, properties).build();
    }

    @Getter
    private static class CasPersistenceUnitInfoDescriptor extends PersistenceUnitInfoDescriptor {
        private final List<String> managedClassNames;

        CasPersistenceUnitInfoDescriptor(final PersistenceUnitInfo info, final List<String> mergedClassesAndPackages) {
            super(info);
            this.managedClassNames = mergedClassesAndPackages;
        }
    }
}
