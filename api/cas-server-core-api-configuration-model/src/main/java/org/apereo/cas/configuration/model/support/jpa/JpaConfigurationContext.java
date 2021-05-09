package org.apereo.cas.configuration.model.support.jpa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.orm.jpa.JpaVendorAdapter;

import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
    private final Map<String, Object> jpaProperties = new LinkedHashMap<>(0);

    @Builder.Default
    private final Set<String> packagesToScan = new LinkedHashSet<>(0);
}
