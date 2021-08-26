package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Abstract class for database authentication handlers.
 *
 * @author Scott Battaglia
 * @since 3.0.0.3
 */
@Getter
public abstract class AbstractJdbcUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final DataSource dataSource;

    protected AbstractJdbcUsernamePasswordAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                                final PrincipalFactory principalFactory,
                                                                final Integer order, final DataSource dataSource) {
        super(name, servicesManager, principalFactory, order);
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
    }
}
