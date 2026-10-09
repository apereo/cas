package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.jpa.JpaConfigurationContext;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.spi.PersistenceProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JpaBeansTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Hibernate")
class JpaBeansTests {
    @Test
    void verifyExplicitManagedTypes() {
        val provider = mock(PersistenceProvider.class);
        when(provider.createContainerEntityManagerFactory(any(), any())).thenReturn(mock(EntityManagerFactory.class));
        val config = JpaConfigurationContext.builder()
            .persistenceUnitName("explicitManagedTypes")
            .persistenceProvider(provider)
            .managedClassNames(Set.of(ManagedEntity.class.getName()))
            .build();
        val bean = JpaBeans.newEntityManagerFactoryBean(config);
        try {
            bean.afterPropertiesSet();
            assertEquals(List.of(ManagedEntity.class.getName()), Objects.requireNonNull(bean.getPersistenceUnitInfo()).getManagedClassNames());
        } finally {
            bean.destroy();
        }
    }

    @Test
    void verifyConnectionValidity() throws Throwable {
        val ds = mock(CloseableDataSource.class);
        when(ds.getConnection()).thenThrow(new RuntimeException());
        assertFalse(JpaBeans.isValidDataSourceConnection(ds, 1));
    }

    @Test
    void verifyPoolingDataSource() throws Throwable {
        val dataSource = JpaBeans.newPoolingDataSource("org.hsqldb.jdbcDriver", "sa",
            StringUtils.EMPTY, "jdbc:hsqldb:mem:" + UUID.randomUUID());
        val pool = (HikariDataSource) dataSource.getTargetDataSource();
        assertEquals(0, pool.getMinimumIdle());
        try (val connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1));
        }
        dataSource.close();
        assertTrue(pool.isClosed());
    }

    @Entity
    static class ManagedEntity {
        @Id
        private Long id;
    }
}
