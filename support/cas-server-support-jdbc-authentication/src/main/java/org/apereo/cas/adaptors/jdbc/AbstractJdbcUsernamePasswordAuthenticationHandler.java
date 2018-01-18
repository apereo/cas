package org.apereo.cas.adaptors.jdbc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Abstract class for database authentication handlers.
 *
 * @author Scott Battaglia
 * @since 3.0.0.3
 */
@Slf4j
@Getter
public abstract class AbstractJdbcUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public AbstractJdbcUsernamePasswordAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                             final PrincipalFactory principalFactory,
                                                             final Integer order, final DataSource dataSource) {
        super(name, servicesManager, principalFactory, order);
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
