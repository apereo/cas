package org.apereo.cas.configuration.model.support.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.orm.jpa.JpaVendorAdapter;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;

/**
 * Simple structure to collect and pass around pieces of JPA config data reusable across
 * different JPA configuration components.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class JpaConfigDataHolder implements Serializable {

    private static final long serialVersionUID = -3940423575751579622L;

    private final transient JpaVendorAdapter jpaVendorAdapter;

    private final String persistenceUnitName;

    private final List<String> packagesToScan;

    private final transient DataSource dataSource;

    public JpaConfigDataHolder(final JpaVendorAdapter jpaVendorAdapter, final String persistenceUnitName, final List<String> packagesToScan) {
        this(jpaVendorAdapter, persistenceUnitName, packagesToScan, null);
    }

}
