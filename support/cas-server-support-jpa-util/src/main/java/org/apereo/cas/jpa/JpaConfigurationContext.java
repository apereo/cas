package org.apereo.cas.jpa;

import module java.base;
import module java.sql;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;
import org.springframework.orm.jpa.JpaVendorAdapter;
import jakarta.persistence.spi.PersistenceProvider;

/**
 * Simple structure to collect and pass around pieces of JPA config data reusable across
 * different JPA configuration components.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Getter
@Setter
@SuperBuilder
public class JpaConfigurationContext {
    private final JpaVendorAdapter jpaVendorAdapter;

    private final String persistenceUnitName;

    private final DataSource dataSource;

    private final PersistenceProvider persistenceProvider;

    @Builder.Default
    private final Map<String, Object> jpaProperties = new LinkedHashMap<>();

    @Builder.Default
    private final Set<String> packagesToScan = new LinkedHashSet<>();
}
