package org.apereo.cas.configuration.model.support.jpa;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaVendorAdapter;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Simple structure to collect and pass around pieces of JPA config data reusable across
 * different JPA configuration components.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class JpaConfigDataHolder implements Serializable {

    private static final long serialVersionUID = -3940423575751579622L;

    private final JpaVendorAdapter jpaVendorAdapter;

    private final String persistenceUnitName;

    private final List<String> packagesToScan;

    private final DataSource dataSource;

    public JpaConfigDataHolder(final JpaVendorAdapter jpaVendorAdapter, final String persistenceUnitName, final List<String> packagesToScan) {
        this(jpaVendorAdapter, persistenceUnitName, packagesToScan, null);
    }

}
