package org.apereo.cas.configuration.model.support.jpa;

import org.springframework.orm.jpa.JpaVendorAdapter;

import javax.sql.DataSource;

/**
 * Simple structure to collect and pass around pieces of JPA config data reusable across
 * different JPA configuration components.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class JpaConfigDataHolder {

    private final JpaVendorAdapter jpaVendorAdapter;

    private final String persistenceUnitName;

    private final String[] packagesToScan;

    private final DataSource dataSource;

    public JpaConfigDataHolder(final JpaVendorAdapter jpaVendorAdapter,
                               final String persistenceUnitName,
                               final String[] packagesToScan,
                               final DataSource dataSource) {
        this.jpaVendorAdapter = jpaVendorAdapter;
        this.persistenceUnitName = persistenceUnitName;
        this.packagesToScan = packagesToScan;
        this.dataSource = dataSource;
    }

    public JpaVendorAdapter getJpaVendorAdapter() {
        return jpaVendorAdapter;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public String[] getPackagesToScan() {
        return packagesToScan;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
