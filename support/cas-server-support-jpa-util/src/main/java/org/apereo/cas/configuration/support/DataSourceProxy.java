package org.apereo.cas.configuration.support;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This purpose of this class is to fix a class loading issue that occurs
 * in some application servers when using a datasource/pool from the
 * container {@link ClassLoader}. By having this class at the app level,
 * proxying that occurs (e.g. {@link org.springframework.cloud.context.config.annotation.RefreshScope} annotation)
 * doesn't encounter
 * problems if another proxying library somewhere else uses the container
 * class loader and encounters other proxying classes in the application.
 * <p>
 * Extends {@link AbstractDataSource} but it could probably
 * just delegate all methods to the wrapped datasource if anything
 * that AbstractDataSource is doing causes a problem.
 *
 * @author Hal Deadman
 * @since 5.1
 */
@RequiredArgsConstructor
public class DataSourceProxy extends AbstractDataSource {

    private final DataSource dataSource;


    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

}
